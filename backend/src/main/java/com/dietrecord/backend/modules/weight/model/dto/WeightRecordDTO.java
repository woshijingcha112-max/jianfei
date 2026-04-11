package com.dietrecord.backend.modules.weight.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WeightRecordDTO(
        @NotNull(message = "weightKg is required")
        BigDecimal weightKg,
        @NotBlank(message = "recordDate is required")
        String recordDate
) {
}
