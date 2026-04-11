package com.dietrecord.backend.modules.period.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PeriodRecordDTO(
        @NotBlank(message = "startDate is required")
        String startDate,
        String endDate,
        String remark
) {
}
