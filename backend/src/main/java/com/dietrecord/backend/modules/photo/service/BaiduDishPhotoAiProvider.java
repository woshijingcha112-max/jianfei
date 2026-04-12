package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiProviderResult;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 百度菜品识别适配器。
 */
@Service
@Slf4j
public class BaiduDishPhotoAiProvider implements PhotoAiProvider {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private volatile CachedAccessToken cachedAccessToken;

    public BaiduDishPhotoAiProvider(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1, appProperties.getPhoto().getAi().getTimeoutMillis())))
                .build();
    }

    @Override
    public String providerCode() {
        return "baidu";
    }

    @Override
    public boolean isAvailable() {
        AppProperties.Baidu baidu = appProperties.getPhoto().getAi().getBaidu();
        return baidu.isEnabled()
                && StringUtils.hasText(baidu.getEndpoint())
                && StringUtils.hasText(baidu.getTokenEndpoint())
                && StringUtils.hasText(baidu.getApiKey())
                && StringUtils.hasText(baidu.getSecretKey());
    }

    @Override
    public PhotoAiProviderResult recognize(PhotoAiRecognitionContext context) throws IOException, InterruptedException {
        if (!isAvailable()) {
            throw new IllegalStateException("baidu ai config is incomplete");
        }

        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();
        AppProperties.Baidu baiduProperties = aiProperties.getBaidu();

        log.info("百度菜品校验开始，请求摘要={}", buildDishRequestSummary(context, baiduProperties));

        long totalStartNanos = System.nanoTime();
        String accessToken = getAccessToken(aiProperties, baiduProperties);
        String requestBody = buildDishRequestBody(baiduProperties, context);

        long dishStartNanos = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baiduProperties.getEndpoint() + "?access_token=" + encode(accessToken)))
                .timeout(Duration.ofMillis(Math.max(1, aiProperties.getTimeoutMillis())))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        long dishElapsedMillis = Duration.ofNanos(System.nanoTime() - dishStartNanos).toMillis();
        long totalElapsedMillis = Duration.ofNanos(System.nanoTime() - totalStartNanos).toMillis();

        log.info("百度菜品校验完成，requestId={}，httpStatus={}，dishElapsedMs={}，totalElapsedMs={}，响应摘要={}",
                context.requestId(),
                response.statusCode(),
                dishElapsedMillis,
                totalElapsedMillis,
                abbreviate(response.body(), 4000));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("baidu dish http status = " + response.statusCode());
        }

        List<PhotoAiRecognitionCandidate> candidates = parseBaiduCandidates(response.body());
        PhotoAiRecognitionCandidate firstCandidate = candidates.isEmpty() ? null : candidates.get(0);
        return new PhotoAiProviderResult(
                providerCode(),
                firstCandidate == null ? null : firstCandidate.foodName(),
                firstCandidate == null ? null : firstCandidate.confidence(),
                null,
                null,
                List.of(),
                firstCandidate == null ? null : firstCandidate.calories(),
                candidates.isEmpty(),
                candidates.isEmpty() ? "百度未返回有效候选结果" : null,
                candidates
        );
    }

    private String getAccessToken(AppProperties.Ai aiProperties, AppProperties.Baidu baiduProperties)
            throws IOException, InterruptedException {
        CachedAccessToken currentToken = cachedAccessToken;
        if (currentToken != null && currentToken.isValid()) {
            return currentToken.value();
        }

        synchronized (this) {
            currentToken = cachedAccessToken;
            if (currentToken != null && currentToken.isValid()) {
                return currentToken.value();
            }

            String tokenRequestBody = "grant_type=client_credentials"
                    + "&client_id=" + encode(baiduProperties.getApiKey())
                    + "&client_secret=" + encode(baiduProperties.getSecretKey());

            log.info("百度 access_token 获取开始，appId={}，tokenEndpoint={}",
                    baiduProperties.getAppId(), baiduProperties.getTokenEndpoint());
            long tokenStartNanos = System.nanoTime();
            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baiduProperties.getTokenEndpoint()))
                    .timeout(Duration.ofMillis(Math.max(1, aiProperties.getTimeoutMillis())))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(
                    tokenRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            long tokenElapsedMillis = Duration.ofNanos(System.nanoTime() - tokenStartNanos).toMillis();
            log.info("百度 access_token 获取完成，appId={}，httpStatus={}，elapsedMs={}，响应摘要={}",
                    baiduProperties.getAppId(),
                    tokenResponse.statusCode(),
                    tokenElapsedMillis,
                    abbreviate(tokenResponse.body(), 1000));
            if (tokenResponse.statusCode() < 200 || tokenResponse.statusCode() >= 300) {
                throw new IllegalStateException("baidu token http status = " + tokenResponse.statusCode());
            }

            JsonNode tokenRoot = objectMapper.readTree(tokenResponse.body());
            if (tokenRoot.hasNonNull("error")) {
                throw new IllegalStateException(
                        tokenRoot.path("error").asText() + " / " + tokenRoot.path("error_description").asText()
                );
            }

            String accessToken = tokenRoot.path("access_token").asText(null);
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("baidu access_token is empty");
            }

            long expiresIn = tokenRoot.path("expires_in").asLong(2_592_000L);
            long refreshBeforeSeconds = Math.max(60L, baiduProperties.getTokenRefreshBeforeSeconds());
            Instant expireAt = Instant.now().plusSeconds(Math.max(60L, expiresIn - refreshBeforeSeconds));

            cachedAccessToken = new CachedAccessToken(accessToken, expireAt);
            log.info("百度 access_token 获取成功，appId={}，有效期截止={}", baiduProperties.getAppId(), expireAt);
            return accessToken;
        }
    }

    private String buildDishRequestBody(AppProperties.Baidu baiduProperties, PhotoAiRecognitionContext context) {
        String imageBase64 = Base64.getEncoder().encodeToString(context.processedPhoto().content());
        return "image=" + encode(imageBase64)
                + "&top_num=" + baiduProperties.getTopNum()
                + "&baike_num=" + baiduProperties.getBaikeNum();
    }

    private List<PhotoAiRecognitionCandidate> parseBaiduCandidates(String responseBody) throws IOException {
        if (!StringUtils.hasText(responseBody)) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(responseBody);
        if (root.hasNonNull("error_code")) {
            throw new IllegalStateException(
                    root.path("error_code").asText() + " / " + root.path("error_msg").asText()
            );
        }

        JsonNode resultNode = root.path("result");
        if (!resultNode.isArray() || resultNode.isEmpty()) {
            return List.of();
        }

        List<PhotoAiRecognitionCandidate> candidates = new ArrayList<>();
        for (JsonNode itemNode : resultNode) {
            String foodName = itemNode.path("name").asText(null);
            if (!StringUtils.hasText(foodName)) {
                continue;
            }

            BigDecimal confidence = readDecimal(itemNode, "probability");
            BigDecimal calories = readDecimal(itemNode, "calorie");

            candidates.add(new PhotoAiRecognitionCandidate(foodName, confidence, null, calories, null));
        }
        return candidates;
    }

    private BigDecimal readDecimal(JsonNode node, String fieldName) {
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isNumber()) {
            return valueNode.decimalValue();
        }

        if (valueNode.isTextual() && StringUtils.hasText(valueNode.asText())) {
            try {
                return new BigDecimal(valueNode.asText().trim());
            } catch (NumberFormatException ex) {
                log.debug("百度字段解析失败，field={}，value={}", fieldName, valueNode.asText());
            }
        }
        return null;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildDishRequestSummary(PhotoAiRecognitionContext context, AppProperties.Baidu baiduProperties)
            throws IOException {
        Map<String, Object> summary = Map.of(
                "requestId", context.requestId(),
                "endpoint", baiduProperties.getEndpoint(),
                "topNum", baiduProperties.getTopNum(),
                "baikeNum", baiduProperties.getBaikeNum(),
                "photoUrl", context.photoUrl(),
                "originalFilename", context.processedPhoto().originalFilename(),
                "imageBytes", context.processedPhoto().sizeBytes(),
                "imageWidth", context.processedPhoto().width(),
                "imageHeight", context.processedPhoto().height()
        );
        return objectMapper.writeValueAsString(summary);
    }

    private String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String normalized = value.replaceAll("[\\r\\n]+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private record CachedAccessToken(String value, Instant expireAt) {

        private boolean isValid() {
            return StringUtils.hasText(value) && expireAt != null && Instant.now().isBefore(expireAt);
        }
    }
}
