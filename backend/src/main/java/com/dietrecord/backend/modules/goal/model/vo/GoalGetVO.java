package com.dietrecord.backend.modules.goal.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalGetVO {

    private BigDecimal currentWeightKg;

    private BigDecimal targetWeightKg;

    private Integer dailyCalorieLimit;
}
