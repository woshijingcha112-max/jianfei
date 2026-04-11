package com.dietrecord.backend.common.dto;

import java.math.BigDecimal;

public record GoalSaveRequest(
        BigDecimal targetWeight,
        Integer dailyCalLimit
) {
}
