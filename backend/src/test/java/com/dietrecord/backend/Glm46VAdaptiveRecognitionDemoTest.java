package com.dietrecord.backend;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoRecognitionOutcome;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.dietrecord.backend.modules.photo.service.AdaptivePhotoAiRecognitionClient;
import com.dietrecord.backend.modules.photo.service.BaiduDishPhotoAiProvider;
import com.dietrecord.backend.modules.photo.service.PhotoRecognitionService;
import com.dietrecord.backend.modules.photo.service.ZhipuPhotoAiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GLM-5V-Turbo + 百度校验最小联调 Demo。
 */
class Glm46VAdaptiveRecognitionDemoTest {

    private static final String ZHIPU_API_KEY_ENV = "PHOTO_AI_ZHIPU_API_KEY";
    private static final String BAIDU_APP_ID_ENV = "PHOTO_AI_BAIDU_APP_ID";
    private static final String BAIDU_API_KEY_ENV = "PHOTO_AI_BAIDU_API_KEY";
    private static final String BAIDU_SECRET_KEY_ENV = "PHOTO_AI_BAIDU_SECRET_KEY";

    @Test
    @DisplayName("GLM-5V-Turbo + 百度校验 Demo")
    void recognizeWithGlmAndBaiduValidation() throws Exception {
        Path imagePath = resolveSinglePicture();
        byte[] imageBytes = Files.readAllBytes(imagePath);
        BufferedImage image = ImageIO.read(imagePath.toFile());
        Assumptions.assumeTrue(image != null, "测试图片无法读取：" + imagePath);

        AppProperties properties = buildProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        FoodLibraryMapper foodLibraryMapper = mock(FoodLibraryMapper.class);
        when(foodLibraryMapper.selectList(any())).thenReturn(mockFoods());

        ZhipuPhotoAiProvider zhipuProvider = new ZhipuPhotoAiProvider(properties, objectMapper);
        BaiduDishPhotoAiProvider baiduProvider = new BaiduDishPhotoAiProvider(properties, objectMapper);
        AdaptivePhotoAiRecognitionClient adaptiveClient = new AdaptivePhotoAiRecognitionClient(
                properties,
                foodLibraryMapper,
                List.of(zhipuProvider, baiduProvider)
        );
        PhotoRecognitionService recognitionService = new PhotoRecognitionService(adaptiveClient, foodLibraryMapper);

        ProcessedPhoto processedPhoto = new ProcessedPhoto(
                imageBytes,
                imagePath.getFileName().toString(),
                "image/jpeg",
                "jpeg",
                false,
                image.getWidth(),
                image.getHeight(),
                imageBytes.length,
                sha256Hex(imageBytes)
        );

        PhotoRecognitionOutcome outcome = recognitionService.recognize(processedPhoto, "/uploads/demo/test.jpeg");
        assertNotNull(outcome.structuredResult(), "结构化识别结果不应为空");
        assertFalse(outcome.recognizedItems().isEmpty(), "识别结果列表不应为空");

        System.out.println("========== GLM-5V-Turbo + 百度校验 Demo ==========");
        System.out.println("imagePath = " + imagePath.toAbsolutePath());
        System.out.println("recognizedItemCount = " + outcome.recognizedItems().size());
        System.out.println("structuredResult = ");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcome.structuredResult()));
    }

    private Path resolveSinglePicture() throws IOException {
        Path pictureDirectory = Path.of("..", "tools", "picture").toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isDirectory(pictureDirectory), "图片目录不存在：" + pictureDirectory);

        List<Path> files = Files.list(pictureDirectory)
                .filter(Files::isRegularFile)
                .toList();
        Assumptions.assumeTrue(!files.isEmpty(), "图片目录下没有可用测试图片：" + pictureDirectory);
        return files.get(0);
    }

    private AppProperties buildProperties() {
        AppProperties properties = new AppProperties();
        properties.getPhoto().getAi().setEnabled(true);
        properties.getPhoto().getAi().setProvider("zhipu");
        properties.getPhoto().getAi().setTimeoutMillis((int) Duration.ofSeconds(45).toMillis());
        properties.getPhoto().getAi().setDefaultMealType("待确认");

        AppProperties.Zhipu zhipu = properties.getPhoto().getAi().getZhipu();
        zhipu.setEnabled(true);
        zhipu.setEndpoint("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        zhipu.setApiKey(readRequiredEnv(ZHIPU_API_KEY_ENV));
        zhipu.setModel("glm-5v-turbo");
        zhipu.setMaxTokens(2048);
        zhipu.setTemperature(0.1d);

        AppProperties.Baidu baidu = properties.getPhoto().getAi().getBaidu();
        baidu.setEnabled(true);
        baidu.setAppId(readRequiredEnv(BAIDU_APP_ID_ENV));
        baidu.setTokenEndpoint("https://aip.baidubce.com/oauth/2.0/token");
        baidu.setEndpoint("https://aip.baidubce.com/rest/2.0/image-classify/v2/dish");
        baidu.setApiKey(readRequiredEnv(BAIDU_API_KEY_ENV));
        baidu.setSecretKey(readRequiredEnv(BAIDU_SECRET_KEY_ENV));
        baidu.setTopNum(5);
        baidu.setBaikeNum(1);
        baidu.setTokenRefreshBeforeSeconds(300);
        return properties;
    }

    private String readRequiredEnv(String key) {
        String value = System.getenv(key);
        Assumptions.assumeTrue(value != null && !value.trim().isEmpty(), "缺少环境变量：" + key);
        return value.trim();
    }

    private List<FoodLibraryPO> mockFoods() {
        return List.of(
                buildFood(1L, "藕片", "蔬菜", 74, 2),
                buildFood(2L, "午餐肉", "肉类加工", 300, 3),
                buildFood(3L, "金针菇", "菌菇", 29, 2),
                buildFood(4L, "麻辣烫", "汤锅类", 106, 4)
        );
    }

    private FoodLibraryPO buildFood(Long id, String foodName, String category, int caloriesKcal, int tagColor) {
        FoodLibraryPO food = new FoodLibraryPO();
        food.setId(id);
        food.setFoodName(foodName);
        food.setCategory(category);
        food.setCaloriesKcal(java.math.BigDecimal.valueOf(caloriesKcal));
        food.setTagColor(tagColor);
        return food;
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前环境不支持 SHA-256", ex);
        }
    }
}
