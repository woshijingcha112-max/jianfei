package com.dietrecord.backend.modules.food.model.dto;

import jakarta.validation.constraints.NotNull;

public record FoodDetailDTO(
        @NotNull(message = "id is required")
        Long id
) {
}
