package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotBlank;

public record DateRequest(
        @NotBlank(message = "date is required")
        String date
) {
}
