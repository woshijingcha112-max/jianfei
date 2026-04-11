package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotBlank;

public record FoodSearchRequest(
        @NotBlank(message = "keyword is required")
        String keyword
) {
}
