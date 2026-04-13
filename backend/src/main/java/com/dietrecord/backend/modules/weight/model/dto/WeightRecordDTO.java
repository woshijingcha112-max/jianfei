package com.dietrecord.backend.modules.weight.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WeightRecordDTO(
        /** 体重千克数 */
        @NotNull(message = "weightKg is required")
        BigDecimal weightKg,
        /** 记录日期 */
        @NotBlank(message = "recordDate is required")
        String recordDate
) {
}
