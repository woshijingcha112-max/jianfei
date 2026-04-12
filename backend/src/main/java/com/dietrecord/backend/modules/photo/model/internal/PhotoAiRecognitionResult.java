package com.dietrecord.backend.modules.photo.model.internal;

import com.dietrecord.backend.modules.photo.model.vo.PhotoStructuredResultVO;

import java.util.List;

/**
 * 图片识别统一输出。
 */
public record PhotoAiRecognitionResult(
        List<PhotoAiRecognitionCandidate> candidates,
        PhotoStructuredResultVO structuredResult
) {

    public PhotoAiRecognitionResult {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
