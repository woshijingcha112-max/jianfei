package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;
import java.util.List;

/**
 * 单个平台识别后的统一结果。
 */
public record PhotoAiProviderResult(
        String providerCode,
        String dishName,
        BigDecimal dishConfidence,
        String dishCategory,
        String sceneDescription,
        List<PhotoAiStructuredIngredient> ingredients,
        BigDecimal totalCalories,
        Boolean needManualConfirm,
        String manualConfirmReason,
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
