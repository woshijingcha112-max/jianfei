package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;

/**
 * 结构化食材明细。
 */
public record PhotoAiStructuredIngredient(
        /** 食材名称 */
        String foodName,
        /** 识别置信度 */
        BigDecimal confidence,
        /** 估算重量克数 */
        BigDecimal weightG,
        /** 估算热量 */
        BigDecimal calories,
        /** 食材分类 */
        String category,
        /** 烹饪状态 */
        String cookingState,
        /** 来源标识 */
        String source
) {
}
