package com.dietrecord.backend.modules.diet.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record DietRecordSaveDTO(
        @NotNull(message = "recordDate is required")
        LocalDate recordDate,
        @NotNull(message = "mealType is required")
        @Min(value = 1, message = "mealType must be between 1 and 4")
        @Max(value = 4, message = "mealType must be between 1 and 4")
        Integer mealType,
        @NotBlank(message = "photoUrl is required")
        String photoUrl,
        @Size(max = 200, message = "remark must be at most 200 characters")
        String remark,
        @NotEmpty(message = "items is required")
        @Valid
        List<DietRecordItemDTO> items
) {
}
