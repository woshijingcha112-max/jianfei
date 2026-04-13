package com.dietrecord.backend.modules.diet.model.vo;

public record TodayDietStatVO(
        /** 日期文案 */
        String dateLabel,
        /** 已摄入热量 */
        Integer consumedCalories,
        /** 目标热量 */
        Integer targetCalories
) {
}
