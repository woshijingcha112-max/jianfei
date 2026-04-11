package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotNull;

public record IdRequest(
        @NotNull(message = "id is required")
        Long id
) {
}
