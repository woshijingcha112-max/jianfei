package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiProviderResult;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionContext;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiStructuredIngredient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 智谱视觉识别适配器。
 */
@Service
@Slf4j
public class ZhipuPhotoAiProvider implements PhotoAiProvider {

    private static final String DEFAULT_SOURCE = "智谱清言";

    private static final String DEFAULT_PROMPT = """
            你是减肥饮食记录场景的图片识别助手。
            你必须基于图片内容，输出一个严格可解析的 JSON 对象。
            不要输出 Markdown，不要输出代码块，不要输出解释说明，不要补充 JSON 之外的任何文字。
            如果有不确定项，可以填写“待确认”或降低置信度，但仍然必须返回 JSON。
            输出结构必须以“图片识别结果”为根节点，并尽量包含以下字段：
            {
              "图片识别结果": {
                "请求编号": "",
                "图片地址": "",
                "识别时间": "",
                "用餐类型": "早餐/午餐/晚餐/加餐/待确认",
                "记录日期": "",
                "整道菜信息": {
                  "菜品名称": "",
                  "菜品置信度": "",
                  "菜品分类": "",
                  "场景描述": ""
                },
                "食材明细": [
                  {
                    "食材名称": "",
                    "识别置信度": "",
                    "估算重量克": "",
                    "估算热量": "",
                    "食材分类": "",
                    "烹饪状态": "",
                    "来源": "智谱清言"
                  }
                ],
                "汇总信息": {
                  "总估算热量": "",
                  "是否需要人工确认": "是/否",
                  "需要人工确认原因": ""
                }
              }
            }
            识别重点：
            1. 优先识别复杂菜品中的具体食材，而不是只说整道菜。
            2. 食材明细尽量细到可用于减肥记录，例如青菜、藕片、午餐肉、金针菇、豆腐、丸类。
            3. 热量和重量允许估算，但不要留空；无法判断时填写保守估值并在人工确认字段说明。
            4. 用餐类型如果无法仅凭图片判断，填写“待确认”。
            5. 来源固定填写“智谱清言”。
            """;

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ZhipuPhotoAiProvider(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1, appProperties.getPhoto().getAi().getTimeoutMillis())))
                .build();
    }

    @Override
    public String providerCode() {
        return "zhipu";
    }

    @Override
    public boolean isAvailable() {
        AppProperties.Zhipu zhipu = appProperties.getPhoto().getAi().getZhipu();
        return zhipu.isEnabled()
                && StringUtils.hasText(zhipu.getEndpoint())
                && StringUtils.hasText(zhipu.getApiKey())
                && StringUtils.hasText(zhipu.getModel());
    }

    @Override
    public PhotoAiProviderResult recognize(PhotoAiRecognitionContext context) throws IOException, InterruptedException {
        if (!isAvailable()) {
            throw new IllegalStateException("zhipu ai config is incomplete");
        }

        AppProperties.Ai aiProperties = appProperties.getPhoto().getAi();
        AppProperties.Zhipu zhipuProperties = aiProperties.getZhipu();

        ObjectNode requestBody = buildRequestBody(context, zhipuProperties);
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        log.info("智谱主识别开始，请求摘要={}", buildRequestSummary(context, zhipuProperties));

        long startNanos = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(zhipuProperties.getEndpoint()))
                .timeout(Duration.ofMillis(Math.max(1, aiProperties.getTimeoutMillis())))
                .header("Authorization", "Bearer " + zhipuProperties.getApiKey())
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.info("智谱主识别完成，requestId={}，httpStatus={}，elapsedMs={}，响应摘要={}",
                    context.requestId(),
                    response.statusCode(),
                    elapsedMillis,
                    abbreviate(response.body(), 4000));
            throw new IllegalStateException("zhipu chat http status = " + response.statusCode() + ", body=" + response.body());
        }

        String responseContent = extractResponseContent(response.body());
        log.info("智谱主识别完成，requestId={}，httpStatus={}，elapsedMs={}，内容摘要={}",
                context.requestId(),
                response.statusCode(),
                elapsedMillis,
                abbreviate(responseContent, 2000));
        return parseResponseContent(responseContent);
    }

    private ObjectNode buildRequestBody(PhotoAiRecognitionContext context, AppProperties.Zhipu zhipuProperties) {
        String imageBase64 = Base64.getEncoder().encodeToString(context.processedPhoto().content());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", zhipuProperties.getModel());
        root.put("temperature", zhipuProperties.getTemperature());
        root.put("max_tokens", Math.max(1024, zhipuProperties.getMaxTokens()));
        root.put("request_id", context.requestId());
        root.put("stream", false);
        root.putObject("thinking").put("type", "disabled");

        ArrayNode messages = root.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", resolvePrompt(zhipuProperties));

        ArrayNode userContent = objectMapper.createArrayNode();
        userContent.addObject()
                .put("type", "image_url")
                .putObject("image_url")
                .put("url", imageBase64);
        userContent.addObject()
                .put("type", "text")
                .put("text", "请严格按约定 JSON 输出这张餐食图片的结构化识别结果。");

        messages.addObject()
                .put("role", "user")
                .set("content", userContent);
        return root;
    }

    private String resolvePrompt(AppProperties.Zhipu zhipuProperties) {
        if (StringUtils.hasText(zhipuProperties.getPrompt())) {
            return zhipuProperties.getPrompt();
        }
        return DEFAULT_PROMPT;
    }

    private String extractResponseContent(String responseBody) throws IOException {
        JsonNode responseNode = objectMapper.readTree(responseBody);
        JsonNode errorNode = responseNode.path("error");
        if (!errorNode.isMissingNode() && !errorNode.isNull()) {
            throw new IllegalStateException(errorNode.toString());
        }

        JsonNode choiceNode = responseNode.path("choices").path(0);
        String contentText = extractMessageContent(choiceNode.path("message"));
        if (!StringUtils.hasText(contentText)) {
            contentText = extractMessageContent(choiceNode);
        }
        if (!StringUtils.hasText(contentText)) {
            throw new IllegalStateException("zhipu response message content is empty");
        }
        return contentText;
    }

    private PhotoAiProviderResult parseResponseContent(String contentText) throws IOException {
        if (!StringUtils.hasText(contentText)) {
            throw new IllegalStateException("zhipu message content is empty");
        }

        JsonNode structuredRoot = objectMapper.readTree(extractJsonText(contentText));
        JsonNode payload = structuredRoot.path("图片识别结果");
        if (payload.isMissingNode() || payload.isNull()) {
            payload = structuredRoot;
        }

        JsonNode dishNode = payload.path("整道菜信息");
        JsonNode summaryNode = payload.path("汇总信息");
        List<PhotoAiStructuredIngredient> ingredients = parseIngredients(payload.path("食材明细"));
        List<PhotoAiRecognitionCandidate> candidates = toCandidates(ingredients, dishNode, summaryNode);

        return new PhotoAiProviderResult(
                providerCode(),
                readText(dishNode, "菜品名称"),
                readDecimal(dishNode, "菜品置信度"),
                readText(dishNode, "菜品分类"),
                readText(dishNode, "场景描述"),
                ingredients,
                readDecimal(summaryNode, "总估算热量"),
                parseManualConfirmFlag(readText(summaryNode, "是否需要人工确认")),
                readText(summaryNode, "需要人工确认原因"),
                candidates
        );
    }

    private String extractMessageContent(JsonNode messageNode) {
        JsonNode contentNode = messageNode.path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode itemNode : contentNode) {
                if (itemNode.isTextual()) {
                    builder.append(itemNode.asText());
                    continue;
                }

                if (itemNode.isObject() && itemNode.path("text").isTextual()) {
                    builder.append(itemNode.path("text").asText());
                }
            }
            return builder.toString();
        }
        return null;
    }

    private String extractJsonText(String contentText) {
        String cleaned = contentText.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalStateException("zhipu content does not contain a valid json object");
        }
        return cleaned.substring(start, end + 1);
    }

    private List<PhotoAiStructuredIngredient> parseIngredients(JsonNode ingredientsNode) {
        if (!ingredientsNode.isArray() || ingredientsNode.isEmpty()) {
            return List.of();
        }

        List<PhotoAiStructuredIngredient> ingredients = new ArrayList<>();
        for (JsonNode itemNode : ingredientsNode) {
            String foodName = readText(itemNode, "食材名称");
            if (!StringUtils.hasText(foodName)) {
                continue;
            }

            ingredients.add(new PhotoAiStructuredIngredient(
                    foodName,
                    readDecimal(itemNode, "识别置信度"),
                    readDecimal(itemNode, "估算重量克"),
                    readDecimal(itemNode, "估算热量"),
                    defaultText(readText(itemNode, "食材分类"), "待确认"),
                    defaultText(readText(itemNode, "烹饪状态"), "待确认"),
                    defaultText(readText(itemNode, "来源"), DEFAULT_SOURCE)
            ));
        }
        return ingredients;
    }

    private List<PhotoAiRecognitionCandidate> toCandidates(List<PhotoAiStructuredIngredient> ingredients,
                                                           JsonNode dishNode,
                                                           JsonNode summaryNode) {
        if (!ingredients.isEmpty()) {
            List<PhotoAiRecognitionCandidate> candidates = new ArrayList<>();
            for (PhotoAiStructuredIngredient ingredient : ingredients) {
                candidates.add(new PhotoAiRecognitionCandidate(
                        ingredient.foodName(),
                        ingredient.confidence(),
                        ingredient.weightG(),
                        ingredient.calories(),
                        null
                ));
            }
            return candidates;
        }

        String dishName = readText(dishNode, "菜品名称");
        if (!StringUtils.hasText(dishName)) {
            return List.of();
        }

        return List.of(new PhotoAiRecognitionCandidate(
                dishName,
                readDecimal(dishNode, "菜品置信度"),
                null,
                readDecimal(summaryNode, "总估算热量"),
                null
        ));
    }

    private Boolean parseManualConfirmFlag(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if ("是".equals(value.trim())) {
            return Boolean.TRUE;
        }
        if ("否".equals(value.trim())) {
            return Boolean.FALSE;
        }
        return null;
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText(null);
        return StringUtils.hasText(value) ? value.trim() : null;
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
                log.debug("智谱字段解析失败，field={}，value={}", fieldName, valueNode.asText());
            }
        }
        return null;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String buildRequestSummary(PhotoAiRecognitionContext context, AppProperties.Zhipu zhipuProperties)
            throws IOException {
        LinkedHashMap<String, Object> summary = new LinkedHashMap<>();
        summary.put("requestId", context.requestId());
        summary.put("endpoint", zhipuProperties.getEndpoint());
        summary.put("model", zhipuProperties.getModel());
        summary.put("timeoutMillis", appProperties.getPhoto().getAi().getTimeoutMillis());
        summary.put("temperature", zhipuProperties.getTemperature());
        summary.put("maxTokens", zhipuProperties.getMaxTokens());
        summary.put("stream", false);
        summary.put("thinkingType", "disabled");
        summary.put("photoUrl", context.photoUrl());
        summary.put("originalFilename", context.processedPhoto().originalFilename());
        summary.put("imageBytes", context.processedPhoto().sizeBytes());
        summary.put("imageWidth", context.processedPhoto().width());
        summary.put("imageHeight", context.processedPhoto().height());
        summary.put("promptPreview", abbreviate(resolvePrompt(zhipuProperties), 300));
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
}
