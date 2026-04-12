package com.dietrecord.backend.modules.goal.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class GoalSaveDTO {

    @NotNull(message = "currentWeight is required")
    @Positive(message = "currentWeight must be positive")
    private BigDecimal currentWeight;

    @NotNull(message = "targetWeight is required")
    @Positive(message = "targetWeight must be positive")
    private BigDecimal targetWeight;

    @NotNull(message = "dailyCalLimit is required")
    @Positive(message = "dailyCalLimit must be positive")
    private Integer dailyCalLimit;
}
