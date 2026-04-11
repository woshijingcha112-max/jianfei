package com.dietrecord.backend.modules.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * 图片识别结果组装服务。
 */
@Service
@Slf4j
public class PhotoRecognitionService {

    private final PhotoAiRecognitionClient photoAiRecognitionClient;
    private final FoodLibraryMapper foodLibraryMapper;

    public PhotoRecognitionService(PhotoAiRecognitionClient photoAiRecognitionClient,
                                   FoodLibraryMapper foodLibraryMapper) {
        this.photoAiRecognitionClient = photoAiRecognitionClient;
        this.foodLibraryMapper = foodLibraryMapper;
    }

    public List<PhotoRecognitionItemVO> recognize(ProcessedPhoto processedPhoto) {
        log.info("开始组装图片识别结果，文件名={}，图片指纹={}",
                processedPhoto.originalFilename(), processedPhoto.sha256());

        // 先获取 AI 候选项，再读取食物库用于标准化匹配。
        List<PhotoAiRecognitionCandidate> candidates = photoAiRecognitionClient.recognize(processedPhoto);
        List<FoodLibraryPO> foods = foodLibraryMapper.selectList(
                new LambdaQueryWrapper<FoodLibraryPO>().orderByAsc(FoodLibraryPO::getId));

        // 按候选项顺序转换为接口返回对象，并过滤掉空名称候选。
        List<PhotoRecognitionItemVO> items = new ArrayList<>();
        int index = 0;
        for (PhotoAiRecognitionCandidate candidate : candidates) {
            if (candidate == null || !StringUtils.hasText(candidate.foodName())) {
                continue;
            }
            items.add(toItem(candidate, foods, index++));
        }

        items.sort(Comparator.comparing(PhotoRecognitionItemVO::getConfidence,
                Comparator.nullsLast(Comparator.reverseOrder())));

        log.info("图片识别结果组装完成，文件名={}，候选数={}，返回数={}",
                processedPhoto.originalFilename(), candidates.size(), items.size());
        return items;
    }

    public List<PhotoRecognitionItemVO> recognize(String sourceFilename) {
        ProcessedPhoto fallbackPhoto = new ProcessedPhoto(
                new byte[0],
                sourceFilename,
                "image/jpeg",
                "jpg",
                false,
                0,
                0,
                0,
                sourceFilename == null ? UUID.randomUUID().toString() : sourceFilename);
        return recognize(fallbackPhoto);
    }

    private PhotoRecognitionItemVO toItem(PhotoAiRecognitionCandidate candidate, List<FoodLibraryPO> foods, int rank) {
        FoodMatch match = findMatch(candidate.foodName(), foods);
        BigDecimal confidence = candidate.confidence() != null
                ? candidate.confidence().setScale(2, RoundingMode.HALF_UP)
                : defaultConfidence(rank);
        BigDecimal weightG = candidate.weightG() != null
                ? candidate.weightG().setScale(1, RoundingMode.HALF_UP)
                : defaultWeight(rank);

        // 优先输出和食物库匹配成功的标准化结果，保证后续保存链路稳定。
        if (match != null) {
            FoodLibraryPO food = match.food();
            BigDecimal calories = calculateCalories(food.getCaloriesKcal(), weightG);
            return new PhotoRecognitionItemVO(
                    buildTempId(rank, food.getId(), food.getFoodName()),
                    food.getId(),
                    food.getFoodName(),
                    calories,
                    food.getTagColor(),
                    Boolean.TRUE,
                    confidence,
                    weightG,
                    Boolean.FALSE
            );
        }

        // 当食物库无法命中时，保留 AI 原始候选，给后续人工修正留空间。
        BigDecimal calories = candidate.calories() != null
                ? candidate.calories().setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        Integer tagColor = candidate.tagColor();
        return new PhotoRecognitionItemVO(
                buildTempId(rank, null, candidate.foodName()),
                null,
                candidate.foodName(),
                calories,
                tagColor,
                Boolean.FALSE,
                confidence,
                weightG,
                Boolean.FALSE
        );
    }

    private FoodMatch findMatch(String candidateName, List<FoodLibraryPO> foods) {
        String normalizedCandidate = normalizeText(candidateName);
        if (!StringUtils.hasText(normalizedCandidate)) {
            return null;
        }

        return foods.stream()
                .map(food -> new FoodMatch(food, score(food, normalizedCandidate)))
                .filter(match -> match.score() > 0)
                .max(Comparator.comparingInt(FoodMatch::score).thenComparing(match -> match.food().getId()))
                .orElse(null);
    }

    private int score(FoodLibraryPO food, String normalizedCandidate) {
        int score = 0;
        String foodName = normalizeText(food.getFoodName());
        if (StringUtils.hasText(foodName) && normalizedCandidate.contains(foodName)) {
            score += 100;
        }

        String foodNameEn = normalizeText(food.getFoodNameEn());
        if (StringUtils.hasText(foodNameEn) && normalizedCandidate.contains(foodNameEn)) {
            score += 60;
        }

        if (StringUtils.hasText(food.getAlias())) {
            for (String alias : food.getAlias().split("[,，]")) {
                String normalizedAlias = normalizeText(alias);
                if (StringUtils.hasText(normalizedAlias) && normalizedCandidate.contains(normalizedAlias)) {
                    score += 80;
                    break;
                }
            }
        }

        String category = normalizeText(food.getCategory());
        if (StringUtils.hasText(category) && normalizedCandidate.contains(category)) {
            score += 20;
        }

        return score;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_\\-./()\\[\\]{}]+", "");
    }

    private BigDecimal calculateCalories(BigDecimal caloriesPer100g, BigDecimal weightG) {
        if (caloriesPer100g == null) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return caloriesPer100g.multiply(weightG).divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultConfidence(int rank) {
        return BigDecimal.valueOf(Math.max(0.55d, 0.78d - (rank * 0.05d))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultWeight(int rank) {
        return BigDecimal.valueOf(Math.max(60L, 120L - (rank * 20L))).setScale(1, RoundingMode.HALF_UP);
    }

    private String buildTempId(int rank, Long foodId, String foodName) {
        String base = (foodId == null ? "unknown" : foodId.toString()) + "-" + rank + "-"
                + Objects.toString(foodName, "null");
        return "tmp-" + UUID.nameUUIDFromBytes(base.getBytes(StandardCharsets.UTF_8));
    }

    private record FoodMatch(FoodLibraryPO food, int score) {
    }
}
