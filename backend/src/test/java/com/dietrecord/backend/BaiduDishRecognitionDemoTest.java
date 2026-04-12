package com.dietrecord.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

/**
 * 百度菜品识别手动联调 Demo。
 *
 * <p>这个测试只用于本地验证百度接口连通性，不进入业务主链路，也不修改任何生产代码。</p>
 * <p>运行时从环境变量读取凭证，并从本地图片路径读取待识别图片。</p>
 */
class BaiduDishRecognitionDemoTest {

    private static final String TOKEN_ENDPOINT = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String DISH_ENDPOINT = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Test
    @DisplayName("百度菜品识别 Demo")
    void recognizeDishFromLocalImage() throws Exception {
        Path imagePath = requireExistingFile(readRequiredEnv("BAIDU_DISH_IMAGE_PATH"));
        byte[] imageBytes = ensureBaiduCompatibleImage(Files.readAllBytes(imagePath));

        String accessToken = resolveAccessToken();
        int topNum = readIntEnv("BAIDU_DISH_TOP_NUM", 5);
        int baikeNum = readIntEnv("BAIDU_DISH_BAIKE_NUM", 1);

        String responseBody = callDishRecognition(accessToken, imageBytes, topNum, baikeNum);
        JsonNode root = OBJECT_MAPPER.readTree(responseBody);

        System.out.println("========== 百度菜品识别 Demo ==========");
        System.out.println("imagePath = " + imagePath.toAbsolutePath());
        System.out.println("topNum = " + topNum + ", baikeNum = " + baikeNum);
        System.out.println("rawResponse = ");
        System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));

        if (root.hasNonNull("error_code")) {
            throw new IllegalStateException(
                    "百度菜品识别接口返回错误："
                            + root.path("error_code").asText()
                            + " / "
                            + root.path("error_msg").asText()
            );
        }

        JsonNode resultNode = root.path("result");
        Assumptions.assumeTrue(
                resultNode.isArray() && resultNode.size() > 0,
                "百度菜品识别没有返回 result 数组，请检查图片内容、账号权限或接口参数。"
        );

        for (int i = 0; i < resultNode.size(); i++) {
            JsonNode item = resultNode.get(i);
            System.out.printf(
                    "候选[%d] name=%s, probability=%s, calorie=%s, has_calorie=%s%n",
                    i + 1,
                    item.path("name").asText(),
                    item.path("probability").asText(),
                    item.path("calorie").asText(),
                    item.path("has_calorie").asText()
            );
        }
    }

    private String resolveAccessToken() throws IOException, InterruptedException {
        String accessToken = readOptionalEnv("BAIDU_AIP_ACCESS_TOKEN");
        if (hasText(accessToken)) {
            return accessToken;
        }

        String apiKey = readRequiredEnv("BAIDU_AIP_API_KEY");
        String secretKey = readRequiredEnv("BAIDU_AIP_SECRET_KEY");
        return fetchAccessToken(apiKey, secretKey);
    }

    private String fetchAccessToken(String apiKey, String secretKey) throws IOException, InterruptedException {
        String requestBody = "grant_type=client_credentials"
                + "&client_id=" + encodeForm(apiKey)
                + "&client_secret=" + encodeForm(secretKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "获取百度 access_token 失败，HTTP 状态码="
                            + response.statusCode()
                            + "，响应="
                            + response.body()
            );
        }

        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        if (root.hasNonNull("error")) {
            throw new IllegalStateException(
                    "获取百度 access_token 失败："
                            + root.path("error").asText()
                            + " / "
                            + root.path("error_description").asText()
            );
        }

        String accessToken = root.path("access_token").asText(null);
        Assumptions.assumeTrue(hasText(accessToken), "百度 access_token 为空，请检查 API Key / Secret Key 是否正确。");
        return accessToken;
    }

    private String callDishRecognition(String accessToken, byte[] imageBytes, int topNum, int baikeNum)
            throws IOException, InterruptedException {
        String requestBody = "image=" + encodeForm(Base64.getEncoder().encodeToString(imageBytes))
                + "&top_num=" + topNum
                + "&baike_num=" + baikeNum;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISH_ENDPOINT + "?access_token=" + encodeQuery(accessToken)))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "百度菜品识别请求失败，HTTP 状态码="
                            + response.statusCode()
                            + "，响应="
                            + response.body()
            );
        }
        return response.body();
    }

    private byte[] ensureBaiduCompatibleImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
            if (image == null) {
                return imageBytes;
            }

            if (Math.min(image.getWidth(), image.getHeight()) >= 15) {
                return imageBytes;
            }

            double scale = 15d / Math.min(image.getWidth(), image.getHeight());
            int targetWidth = Math.max(15, (int) Math.round(image.getWidth() * scale));
            int targetHeight = Math.max(15, (int) Math.round(image.getHeight() * scale));

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resizedImage.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            } finally {
                graphics.dispose();
            }

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("处理 demo 图片失败", ex);
        }
    }

    private Path requireExistingFile(String pathValue) {
        Path path = Path.of(pathValue).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.exists(path), "图片路径不存在：" + path);
        Assumptions.assumeTrue(Files.isRegularFile(path), "图片路径不是普通文件：" + path);
        return path;
    }

    private String readRequiredEnv(String key) {
        String value = readOptionalEnv(key);
        Assumptions.assumeTrue(hasText(value), "缺少环境变量：" + key);
        return value.trim();
    }

    private String readOptionalEnv(String key) {
        String value = System.getenv(key);
        return value == null ? null : value.trim();
    }

    private int readIntEnv(String key, int defaultValue) {
        String value = readOptionalEnv(key);
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("环境变量 " + key + " 不是有效整数：" + value, ex);
        }
    }

    private String encodeQuery(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encodeForm(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
