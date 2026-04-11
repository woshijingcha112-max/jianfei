package com.dietrecord.backend.modules.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 自适应图片识别客户端实现。
 */
@Service
@Slf4j
public class AdaptivePhotoAiRecognitionClient implements PhotoAiRecognitionClient {

    private static final String FIXED_PROMPT = """
            你是减肥饮食拍照识别助手。请根据图片识别餐食，并且只输出严格 JSON 数组，不要输出 Markdown、解释或多余文字。
            每个数组元素必须包含：
            - foodName: 标准化中文食物名，尽量贴近菜品库常见名称
            - confidence: 0 到 1 的小数
            - weightG: 估算克重，数值
            - calories: 估算热量，数值，可为空
            - tagColor: 可选，1=绿 2=橙 3=红
            - matchedHint: 可选，能与菜品库匹配时填 true
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
        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();

        // 当前只在显式启用且 provider 为 duijieai 时走真实外调。
        if (!aiProperties.isEnabled()
                || !StringUtils.hasText(aiProperties.getProvider())
                || !"duijieai".equalsIgnoreCase(aiProperties.getProvider())) {
            log.info("AI 外调未启用或 provider 不匹配，回退本地候选，文件名={}", processedPhoto.originalFilename());
            return fallbackRecognize(processedPhoto);
        }

        try {
            List<PhotoAiRecognitionCandidate> candidates = invokeAiPlatform(processedPhoto);
            if (!candidates.isEmpty()) {
                log.info("AI 外调识别成功，文件名={}，候选数={}", processedPhoto.originalFilename(), candidates.size());
                return candidates;
            }
            log.warn("AI 外调返回空结果，回退本地候选，文件名={}", processedPhoto.originalFilename());
        } catch (Exception ex) {
            log.warn("AI 外调识别失败，回退本地候选，文件名={}，原因={}",
                    processedPhoto.originalFilename(), ex.getMessage());
        }
        return fallbackRecognize(processedPhoto);
    }

    private List<PhotoAiRecognitionCandidate> invokeAiPlatform(ProcessedPhoto processedPhoto)
            throws IOException, InterruptedException {
        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();
        if (!StringUtils.hasText(aiProperties.getEndpoint()) || !StringUtils.hasText(aiProperties.getModel())) {
            log.warn("AI 外调配置不完整，直接回退本地候选，endpoint={}，model={}",
                    aiProperties.getEndpoint(), aiProperties.getModel());
            return fallbackRecognize(processedPhoto);
        }

        // 请求体、鉴权头和响应解析集中放在这里，方便后续按平台真实契约调整。
        String requestBody = buildRequestBody(aiProperties.getModel(), processedPhoto);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiProperties.getEndpoint()))
                .timeout(Duration.ofMillis(Math.max(1, aiProperties.getTimeoutMillis())))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
            log.warn("AI 响应解析失败，回退空候选等待上层处理，原因={}", ex.getMessage());
            return List.of();
        }
    }

    private List<JsonNode> extractItemNodes(JsonNode root) {
        List<JsonNode> nodes = new ArrayList<>();
        if (root == null) {
            return nodes;
        }

        // 优先处理数组形态响应，兼容直接返回候选列表的场景。
        if (root.isArray()) {
            root.forEach(nodes::add);
            return nodes;
        }

        // 再兼容常见包装字段，降低对具体平台返回结构的耦合。
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

        // 最后兼容大模型 choices 包装，把文本中的 JSON 继续拆出来。
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
                        log.debug("choices 文本内容不是可解析 JSON，已忽略该片段");
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
                    log.debug("字段 {} 无法解析为小数，原值={}", fieldName, field.asText());
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
                    log.debug("字段 {} 无法解析为整数，原值={}", fieldName, field.asText());
                }
            }
        }
        return null;
    }

    private List<PhotoAiRecognitionCandidate> fallbackRecognize(ProcessedPhoto processedPhoto) {
        List<FoodLibraryPO> foods = foodLibraryMapper.selectList(
                new LambdaQueryWrapper<FoodLibraryPO>().orderByAsc(FoodLibraryPO::getId));
        if (foods.isEmpty()) {
            log.warn("食物库为空，无法生成本地回退候选，文件名={}", processedPhoto.originalFilename());
            return List.of();
        }

        // 使用图片指纹做稳定种子，保证同一张图在回退模式下结果尽量稳定。
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

        log.info("本地回退候选生成完成，文件名={}，候选数={}", processedPhoto.originalFilename(), candidates.size());
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
