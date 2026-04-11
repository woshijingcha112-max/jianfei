package com.dietrecord.backend.modules.photo.model.vo;

import java.math.BigDecimal;

public class PhotoRecognitionItemVO {

    private final String tempId;
    private final Long foodId;
    private final String foodName;
    private final BigDecimal calories;
    private final Integer tagColor;
    private final Boolean matched;
    private final BigDecimal confidence;
    private final BigDecimal weightG;
    private final Boolean isConfirmed;

    public PhotoRecognitionItemVO(String tempId, Long foodId, String foodName, BigDecimal calories, Integer tagColor,
                                  Boolean matched, BigDecimal confidence, BigDecimal weightG, Boolean isConfirmed) {
        this.tempId = tempId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.calories = calories;
        this.tagColor = tagColor;
        this.matched = matched;
        this.confidence = confidence;
        this.weightG = weightG;
        this.isConfirmed = isConfirmed;
    }

    public String getTempId() {
        return tempId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public Integer getTagColor() {
        return tagColor;
    }

    public Boolean getMatched() {
        return matched;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public BigDecimal getWeightG() {
        return weightG;
    }

    public Boolean getIsConfirmed() {
        return isConfirmed;
    }
}
