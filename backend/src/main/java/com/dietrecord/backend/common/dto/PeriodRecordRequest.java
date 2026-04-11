package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotBlank;

public record PeriodRecordRequest(
        @NotBlank(message = "startDate is required")
        String startDate,
        String endDate,
        String remark
) {
}
