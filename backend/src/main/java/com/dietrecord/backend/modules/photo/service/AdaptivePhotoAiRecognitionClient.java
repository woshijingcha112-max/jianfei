package com.dietrecord.backend.modules.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AdaptivePhotoAiRecognitionClient implements PhotoAiRecognitionClient {

    private static final Logger log = LoggerFactory.getLogger(AdaptivePhotoAiRecognitionClient.class);

    private static final String FIXED_PROMPT = """
            你是减肥餐拍照识别助手。请根据图片识别餐食，并只输出严格 JSON 数组，不要输出 Markdown、解释或多余文字。
            每个数组元素必须包含：
            - foodName: 标准化中文食物名，尽量贴近菜单库常见名称
            - confidence: 0 到 1 的小数
            - weightG: 估算克重，数字
            - calories: 估算热量，数字，可为空
            - tagColor: 可选，1=绿 2=橙 3=红
            - matchedHint: 可选，能与菜单库匹配时填 true
            如果看不清，请返回最有可能的候选，但不要编造过多项。
            示例：
            [{"foodName":"番茄炒蛋","confidence":0.92,"weightG":180,"calories":320,"tagColor":2,"matchedHint":true}]
            """;

    private final AppProperties appProperties;
    private final FoodLibraryMapper foodLibraryMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AdaptivePhotoAiRecognitionClient(AppProperties appProperties,
                                            FoodLibraryMapper foodLibraryMapper,
                                            ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.foodLibraryMapper = foodLibraryMapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1, appProperties.getPhoto().getAi().getTimeoutMillis())))
                .build();
    }

    @Override
    public List<PhotoAiRecognitionCandidate> recognize(ProcessedPhoto processedPhoto) {
        if (!appProperties.getPhoto().getAi().isEnabled()
                || !StringUtils.hasText(appProperties.getPhoto().getAi().getProvider())
                || !"duijieai".equalsIgnoreCase(appProperties.getPhoto().getAi().getProvider())) {
            return fallbackRecognize(processedPhoto);
        }

        try {
            List<PhotoAiRecognitionCandidate> candidates = invokeAiPlatform(processedPhoto);
            if (!candidates.isEmpty()) {
                return candidates;
            }
            log.warn("ai provider returned empty recognition result, falling back to local mock");
        } catch (Exception ex) {
            log.warn("ai provider recognition failed, falling back to local mock: {}", ex.getMessage());
        }
        return fallbackRecognize(processedPhoto);
    }

    private List<PhotoAiRecognitionCandidate> invokeAiPlatform(ProcessedPhoto processedPhoto) throws IOException, InterruptedException {
        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();
        if (!StringUtils.hasText(aiProperties.getEndpoint()) || !StringUtils.hasText(aiProperties.getModel())) {
            return fallbackRecognize(processedPhoto);
        }

        // TODO: Replace the request contract below with the exact duijieai platform HTTP format.
        // Keep this block isolated so the endpoint, headers and payload can be edited manually.
        String requestBody = buildRequestBody(aiProperties.getModel(), processedPhoto);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiProperties.getEndpoint()))
                .timeout(Duration.ofMillis(Math.max(1, aiProperties.getTimeoutMillis())))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("ai platform returned status " + response.statusCode());
        }
        return parseCandidates(response.body());
    }

    private String buildRequestBody(String model, ProcessedPhoto processedPhoto) {
        String imageBase64 = Base64.getEncoder().encodeToString(processedPhoto.content());
        return """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "image": "%s",
                  "imageContentType": "%s",
                  "fileName": "%s",
                  "compressed": %s,
                  "width": %d,
                  "height": %d,
                  "sizeBytes": %d
                }
                """.formatted(
                escapeJson(model),
                escapeJson(FIXED_PROMPT),
                imageBase64,
                escapeJson(processedPhoto.contentType()),
                escapeJson(Objects.toString(processedPhoto.originalFilename(), "")),
                processedPhoto.compressed(),
                processedPhoto.width(),
                processedPhoto.height(),
                processedPhoto.sizeBytes());
    }

    private List<PhotoAiRecognitionCandidate> parseCandidates(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<JsonNode> itemNodes = extractItemNodes(root);
            List<PhotoAiRecognitionCandidate> candidates = new ArrayList<>();
            for (JsonNode itemNode : itemNodes) {
                PhotoAiRecognitionCandidate candidate = parseCandidate(itemNode);
                if (candidate != null && StringUtils.hasText(candidate.foodName())) {
                    candidates.add(candidate);
                }
            }
            return candidates;
        } catch (Exception ex) {
            log.warn("failed to parse ai response, falling back to mock: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<JsonNode> extractItemNodes(JsonNode root) {
        List<JsonNode> nodes = new ArrayList<>();
        if (root == null) {
            return nodes;
        }
        if (root.isArray()) {
            root.forEach(nodes::add);
            return nodes;
        }
        if (root.has("items") && root.get("items").isArray()) {
            root.get("items").forEach(nodes::add);
            return nodes;
        }
        if (root.has("data") && root.get("data").isArray()) {
            root.get("data").forEach(nodes::add);
            return nodes;
        }
        if (root.has("result") && root.get("result").isArray()) {
            root.get("result").forEach(nodes::add);
            return nodes;
        }
        if (root.has("choices") && root.get("choices").isArray()) {
            root.get("choices").forEach(choice -> {
                JsonNode content = choice.path("message").path("content");
                if (content.isArray()) {
                    content.forEach(nodes::add);
                } else if (content.isTextual()) {
                    try {
                        JsonNode parsed = objectMapper.readTree(content.asText());
                        if (parsed.isArray()) {
                            parsed.forEach(nodes::add);
                        } else if (parsed.isObject()) {
                            nodes.add(parsed);
                        }
                    } catch (Exception ignore) {
                        // Keep parsing lenient for platform responses that wrap JSON in text.
                    }
                }
            });
            return nodes;
        }
        if (root.isObject()) {
            nodes.add(root);
        }
        return nodes;
    }

    private PhotoAiRecognitionCandidate parseCandidate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String foodName = firstText(node, "foodName", "name", "label", "title", "dishName", "food_name");
        if (!StringUtils.hasText(foodName)) {
            return null;
        }
        BigDecimal confidence = firstDecimal(node, "confidence", "score", "probability");
        BigDecimal weightG = firstDecimal(node, "weightG", "estimatedWeightG", "weight", "grams");
        BigDecimal calories = firstDecimal(node, "calories", "estimatedCalories", "kcal");
        Integer tagColor = firstInteger(node, "tagColor", "color");
        return new PhotoAiRecognitionCandidate(foodName, confidence, weightG, calories, tagColor);
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && field.isTextual() && StringUtils.hasText(field.asText())) {
                return field.asText().trim();
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && field.isNumber()) {
                return field.decimalValue();
            }
            if (field != null && field.isTextual() && StringUtils.hasText(field.asText())) {
                try {
                    return new BigDecimal(field.asText().trim());
                } catch (NumberFormatException ignore) {
                    // continue
                }
            }
        }
        return null;
    }

    private Integer firstInteger(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && field.canConvertToInt()) {
                return field.asInt();
            }
            if (field != null && field.isTextual() && StringUtils.hasText(field.asText())) {
                try {
                    return Integer.parseInt(field.asText().trim());
                } catch (NumberFormatException ignore) {
                    // continue
                }
            }
        }
        return null;
    }

    private List<PhotoAiRecognitionCandidate> fallbackRecognize(ProcessedPhoto processedPhoto) {
        List<FoodLibraryPO> foods = foodLibraryMapper.selectList(
                new LambdaQueryWrapper<FoodLibraryPO>().orderByAsc(FoodLibraryPO::getId));
        if (foods.isEmpty()) {
            return List.of();
        }

        int candidateCount = Math.min(3, foods.size());
        long seed = decodeSeed(processedPhoto.sha256());
        int startIndex = (int) Math.floorMod(seed, foods.size());
        List<PhotoAiRecognitionCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < candidateCount; i++) {
            FoodLibraryPO food = foods.get((startIndex + i) % foods.size());
            BigDecimal confidence = BigDecimal.valueOf(Math.max(0.55d, 0.78d - (i * 0.07d)));
            BigDecimal weightG = BigDecimal.valueOf(Math.max(60L, 120L - (i * 20L)));
            candidates.add(new PhotoAiRecognitionCandidate(
                    food.getFoodName(),
                    confidence,
                    weightG,
                    null,
                    food.getTagColor()));
        }
        return candidates;
    }

    private long decodeSeed(String value) {
        if (!StringUtils.hasText(value)) {
            return UUID.randomUUID().getMostSignificantBits();
        }
        long hash = 1125899906842597L;
        for (int i = 0; i < value.length(); i++) {
            hash = 31 * hash + value.charAt(i);
        }
        return hash;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
