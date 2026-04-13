package com.dietrecord.backend.modules.goal.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalGetVO {

    /** 当前体重 */
    private BigDecimal currentWeightKg;

    /** 目标体重 */
    private BigDecimal targetWeightKg;

    /** 每日热量上限 */
    private Integer dailyCalorieLimit;
}
