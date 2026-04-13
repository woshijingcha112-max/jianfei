package com.dietrecord.backend.modules.food.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FoodSearchDTO(
        /** 搜索关键字 */
        @NotBlank(message = "keyword is required")
        String keyword
) {
}
