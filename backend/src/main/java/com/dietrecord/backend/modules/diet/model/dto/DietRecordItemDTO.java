package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;

public record DietRecordItemDTO(
        Long foodId,
        @NotBlank(message = "foodName is required")
        String foodName,
        BigDecimal weightG,
        @NotNull(message = "calories is required")
        BigDecimal calories,
        @NotNull(message = "tagColor is required")
        @Min(value = 1, message = "tagColor must be between 1 and 3")
        @Max(value = 3, message = "tagColor must be between 1 and 3")
        Integer tagColor,
        Integer isConfirmed
) {
}
