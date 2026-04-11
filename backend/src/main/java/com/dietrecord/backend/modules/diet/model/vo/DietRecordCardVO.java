package com.dietrecord.backend.modules.diet.model.vo;

public record DietRecordCardVO(
        Long id,
        String savedAt,
        String summary,
        Integer totalCalories,
        Integer dominantTag
) {
}
