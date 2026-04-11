package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;

public record PhotoAiRecognitionCandidate(
        String foodName,
        BigDecimal confidence,
        BigDecimal weightG,
        BigDecimal calories,
        Integer tagColor
) {
}
