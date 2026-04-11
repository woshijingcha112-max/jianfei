package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DietRecordSaveRequest(
        @NotBlank(message = "recordDate is required")
        String recordDate,
        @NotNull(message = "mealType is required")
        Integer mealType,
        String remark
) {
}
