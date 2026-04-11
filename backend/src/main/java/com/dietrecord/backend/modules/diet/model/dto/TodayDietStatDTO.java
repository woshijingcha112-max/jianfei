package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TodayDietStatDTO(
        @NotNull(message = "date is required")
        LocalDate date
) {
}
