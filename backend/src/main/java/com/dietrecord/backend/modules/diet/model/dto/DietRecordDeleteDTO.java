package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotNull;

public record DietRecordDeleteDTO(
        /** 饮食记录主键 */
        @NotNull(message = "recordId is required")
        Long recordId
) {
}
