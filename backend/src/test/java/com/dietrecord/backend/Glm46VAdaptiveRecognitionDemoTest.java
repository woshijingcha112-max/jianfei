package com.dietrecord.backend;

import com.dietrecord.backend.config.AppProperties;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoRecognitionOutcome;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.dietrecord.backend.modules.photo.service.AdaptivePhotoAiRecognitionClient;
import com.dietrecord.backend.modules.photo.service.PhotoRecognitionService;
import com.dietrecord.backend.modules.photo.service.ZhipuPhotoAiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Glm46VAdaptiveRecognitionDemoTest {

    @Test
    @DisplayName("GLM-4.6V smoke demo")
    void recognizeWithGlm46v() throws Exception {
        Path imagePath = resolveSinglePicture();
        byte[] imageBytes = Files.readAllBytes(imagePath);
        BufferedImage image = ImageIO.read(imagePath.toFile());
        Assumptions.assumeTrue(image != null, "测试图片无法读取: " + imagePath);

        AppProperties properties = loadZhipuOnlyProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        FoodLibraryMapper foodLibraryMapper = mock(FoodLibraryMapper.class);
        when(foodLibraryMapper.selectList(any())).thenReturn(mockFoods());

        ZhipuPhotoAiProvider zhipuProvider = new ZhipuPhotoAiProvider(properties, objectMapper);
        AdaptivePhotoAiRecognitionClient adaptiveClient = new AdaptivePhotoAiRecognitionClient(
                properties,
                foodLibraryMapper,
                List.of(zhipuProvider)
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
        assertNotNull(outcome.structuredResult(), "structured result should not be null");
        assertFalse(outcome.recognizedItems().isEmpty(), "recognized items should not be empty");

        System.out.println("========== GLM-4.6V Smoke Demo ==========");
        System.out.println("imagePath = " + imagePath.toAbsolutePath());
        System.out.println("model = " + properties.getPhoto().getAi().getZhipu().getModel());
        System.out.println("recognizedItemCount = " + outcome.recognizedItems().size());
    }

    private Path resolveSinglePicture() throws IOException {
        Path pictureDirectory = Path.of("..", "tools", "picture").toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isDirectory(pictureDirectory), "图片目录不存在: " + pictureDirectory);

        try (Stream<Path> files = Files.list(pictureDirectory)) {
            Path imagePath = files
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElse(null);
            Assumptions.assumeTrue(imagePath != null, "图片目录下没有可用测试图片: " + pictureDirectory);
            return imagePath;
        }
    }

    private AppProperties loadZhipuOnlyProperties() {
        Properties yaml = loadYamlProperties();
        AppProperties properties = new AppProperties();
        properties.getPhoto().getAi().setEnabled(true);
        properties.getPhoto().getAi().setProvider("zhipu");
        properties.getPhoto().getAi().setTimeoutMillis((int) Duration.ofSeconds(45).toMillis());
        properties.getPhoto().getAi().setDefaultMealType("待确认");
        properties.getPhoto().getAi().getBaidu().setEnabled(false);

        AppProperties.Zhipu zhipu = properties.getPhoto().getAi().getZhipu();
        zhipu.setEnabled(true);
        zhipu.setEndpoint(requireProperty(yaml, "app.photo.ai.zhipu.endpoint"));
        zhipu.setApiKey(requireProperty(yaml, "app.photo.ai.zhipu.api-key"));
        zhipu.setModel(requireProperty(yaml, "app.photo.ai.zhipu.model"));
        zhipu.setMaxTokens(Integer.parseInt(requireProperty(yaml, "app.photo.ai.zhipu.max-tokens")));
        zhipu.setTemperature(Double.parseDouble(requireProperty(yaml, "app.photo.ai.zhipu.temperature")));
        zhipu.setPrompt(requireProperty(yaml, "app.photo.ai.zhipu.prompt"));
        return properties;
    }

    private Properties loadYamlProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application.yml"));
        Properties properties = factory.getObject();
        Assumptions.assumeTrue(properties != null, "application.yml 未能加载");
        return properties;
    }

    private String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        Assumptions.assumeTrue(value != null && !value.trim().isEmpty(), "缺少配置项: " + key);
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
        food.setCaloriesKcal(BigDecimal.valueOf(caloriesKcal));
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
