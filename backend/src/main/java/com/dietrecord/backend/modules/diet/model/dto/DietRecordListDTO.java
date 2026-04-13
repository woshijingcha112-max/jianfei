package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DietRecordListDTO(
        /** 查询日期 */
        @NotNull(message = "date is required")
        LocalDate date
) {
}
