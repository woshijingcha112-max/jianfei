package com.dietrecord.backend.modules.photo.model.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图片识别上下文。
 */
public record PhotoAiRecognitionContext(
        String requestId,
        ProcessedPhoto processedPhoto,
        String photoUrl,
        String mealType,
        LocalDate recordDate,
        LocalDateTime recognizedAt
) {
}
