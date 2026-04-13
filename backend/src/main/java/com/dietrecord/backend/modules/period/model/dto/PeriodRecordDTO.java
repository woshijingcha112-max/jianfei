package com.dietrecord.backend.modules.period.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PeriodRecordDTO(
        /** 开始日期 */
        @NotBlank(message = "startDate is required")
        String startDate,
        /** 结束日期 */
        String endDate,
        /** 备注 */
        String remark
) {
}
