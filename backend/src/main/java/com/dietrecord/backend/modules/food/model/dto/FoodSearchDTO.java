package com.dietrecord.backend.modules.food.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FoodSearchDTO(
        @NotBlank(message = "keyword is required")
        String keyword
) {
}
