package com.dietrecord.backend.modules.diet.model.vo;

public record TodayDietStatVO(
        String dateLabel,
        Integer consumedCalories,
        Integer targetCalories
) {
}
