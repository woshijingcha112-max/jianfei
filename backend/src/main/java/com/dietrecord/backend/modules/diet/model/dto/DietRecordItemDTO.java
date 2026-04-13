package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;

public record DietRecordItemDTO(
        /** 食物库主键 */
        Long foodId,
        /** 食物名称 */
        @NotBlank(message = "foodName is required")
        String foodName,
        /** 估算重量克数 */
        BigDecimal weightG,
        /** 估算热量 */
        @NotNull(message = "calories is required")
        BigDecimal calories,
        /** 标签颜色等级 */
        @NotNull(message = "tagColor is required")
        @Min(value = 1, message = "tagColor must be between 1 and 3")
        @Max(value = 3, message = "tagColor must be between 1 and 3")
        Integer tagColor,
        /** 是否人工确认 */
        Integer isConfirmed
) {
}
