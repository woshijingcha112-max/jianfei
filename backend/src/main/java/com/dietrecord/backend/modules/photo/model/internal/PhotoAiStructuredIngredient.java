package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;

/**
 * 结构化食材明细。
 */
public record PhotoAiStructuredIngredient(
        String foodName,
        BigDecimal confidence,
        BigDecimal weightG,
        BigDecimal calories,
        String category,
        String cookingState,
        String source
) {
}
