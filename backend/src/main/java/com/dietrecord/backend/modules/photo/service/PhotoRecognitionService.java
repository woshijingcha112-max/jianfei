package com.dietrecord.backend.modules.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dietrecord.backend.modules.food.mapper.FoodLibraryMapper;
import com.dietrecord.backend.modules.food.model.po.FoodLibraryPO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PhotoRecognitionService {

    private final FoodLibraryMapper foodLibraryMapper;

    public PhotoRecognitionService(FoodLibraryMapper foodLibraryMapper) {
        this.foodLibraryMapper = foodLibraryMapper;
    }

    public List<PhotoRecognitionItemVO> recognize(String sourceFilename) {
        List<FoodLibraryPO> foods = foodLibraryMapper.selectList(
                new LambdaQueryWrapper<FoodLibraryPO>().orderByAsc(FoodLibraryPO::getId));
        if (foods.isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedSource = normalizeText(sourceFilename);
        List<ScoredFood> rankedFoods = foods.stream()
                .map(food -> new ScoredFood(food, score(food, normalizedSource)))
                .sorted(Comparator.comparingInt(ScoredFood::score).reversed()
                        .thenComparing(scoredFood -> scoredFood.food().getId()))
                .limit(3)
                .collect(Collectors.toList());

        AtomicInteger index = new AtomicInteger();
        return rankedFoods.stream()
                .map(scoredFood -> toItem(scoredFood, index.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private PhotoRecognitionItemVO toItem(ScoredFood scoredFood, int rank) {
        FoodLibraryPO food = scoredFood.food();
        int score = scoredFood.score();
        BigDecimal weightG = determineWeight(score, rank);
        BigDecimal calories = calculateCalories(food.getCaloriesKcal(), weightG);
        BigDecimal confidence = determineConfidence(score, rank);

        return new PhotoRecognitionItemVO(
                "tmp-" + UUID.randomUUID().toString().replace("-", "") + "-" + (rank + 1),
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

    private BigDecimal determineWeight(int score, int rank) {
        long weight;
        if (score >= 120) {
            if (rank == 0) {
                weight = 160L;
            } else if (rank == 1) {
                weight = 120L;
            } else {
                weight = 90L;
            }
        } else if (score >= 80) {
            if (rank == 0) {
                weight = 140L;
            } else if (rank == 1) {
                weight = 100L;
            } else {
                weight = 70L;
            }
        } else if (score > 0) {
            if (rank == 0) {
                weight = 120L;
            } else if (rank == 1) {
                weight = 90L;
            } else {
                weight = 60L;
            }
        } else {
            if (rank == 0) {
                weight = 110L;
            } else if (rank == 1) {
                weight = 80L;
            } else {
                weight = 60L;
            }
        }
        return BigDecimal.valueOf(weight);
    }

    private BigDecimal determineConfidence(int score, int rank) {
        double confidence;
        if (score >= 120) {
            if (rank == 0) {
                confidence = 0.96d;
            } else if (rank == 1) {
                confidence = 0.91d;
            } else {
                confidence = 0.88d;
            }
        } else if (score >= 80) {
            if (rank == 0) {
                confidence = 0.90d;
            } else if (rank == 1) {
                confidence = 0.85d;
            } else {
                confidence = 0.80d;
            }
        } else if (score > 0) {
            if (rank == 0) {
                confidence = 0.82d;
            } else if (rank == 1) {
                confidence = 0.76d;
            } else {
                confidence = 0.72d;
            }
        } else {
            if (rank == 0) {
                confidence = 0.74d;
            } else if (rank == 1) {
                confidence = 0.68d;
            } else {
                confidence = 0.64d;
            }
        }
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCalories(BigDecimal caloriesPer100g, BigDecimal weightG) {
        if (caloriesPer100g == null) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return caloriesPer100g
                .multiply(weightG)
                .divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    private int score(FoodLibraryPO food, String normalizedSource) {
        if (!StringUtils.hasText(normalizedSource)) {
            return 0;
        }

        int score = 0;
        String foodName = normalizeText(food.getFoodName());
        if (StringUtils.hasText(foodName) && normalizedSource.contains(foodName)) {
            score += 100;
        }

        String foodNameEn = normalizeText(food.getFoodNameEn());
        if (StringUtils.hasText(foodNameEn) && normalizedSource.contains(foodNameEn)) {
            score += 60;
        }

        if (StringUtils.hasText(food.getAlias())) {
            for (String alias : food.getAlias().split("[,，]")) {
                String normalizedAlias = normalizeText(alias);
                if (StringUtils.hasText(normalizedAlias) && normalizedSource.contains(normalizedAlias)) {
                    score += 80;
                    break;
                }
            }
        }

        String category = normalizeText(food.getCategory());
        if (StringUtils.hasText(category) && normalizedSource.contains(category)) {
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

    private static final class ScoredFood {
        private final FoodLibraryPO food;
        private final int score;

        private ScoredFood(FoodLibraryPO food, int score) {
            this.food = food;
            this.score = score;
        }

        private FoodLibraryPO food() {
            return food;
        }

        private int score() {
            return score;
        }
    }
}
