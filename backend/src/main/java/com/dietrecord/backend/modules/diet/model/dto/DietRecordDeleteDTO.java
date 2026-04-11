package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotNull;

public record DietRecordDeleteDTO(
        @NotNull(message = "recordId is required")
        Long recordId
) {
}
