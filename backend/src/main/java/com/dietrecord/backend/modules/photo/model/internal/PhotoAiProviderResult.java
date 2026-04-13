package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;
import java.util.List;

/**
 * 单个平台识别后的统一结果。
 */
public record PhotoAiProviderResult(
        /** 平台编码 */
        String providerCode,
        /** 菜品名称 */
        String dishName,
        /** 菜品置信度 */
        BigDecimal dishConfidence,
        /** 菜品分类 */
        String dishCategory,
        /** 场景描述 */
        String sceneDescription,
        /** 结构化食材明细 */
        List<PhotoAiStructuredIngredient> ingredients,
        /** 总估算热量 */
        BigDecimal totalCalories,
        /** 是否需要人工确认 */
        Boolean needManualConfirm,
        /** 人工确认原因 */
        String manualConfirmReason,
        /** 候选识别结果 */
        List<PhotoAiRecognitionCandidate> candidates
) {

    public PhotoAiProviderResult {
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    public boolean hasCandidates() {
        return !candidates.isEmpty();
    }

    public boolean hasIngredients() {
        return !ingredients.isEmpty();
    }
}
