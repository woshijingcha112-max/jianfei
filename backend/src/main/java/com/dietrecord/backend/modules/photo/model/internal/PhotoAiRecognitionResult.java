package com.dietrecord.backend.modules.photo.model.internal;

import com.dietrecord.backend.modules.photo.model.vo.PhotoStructuredResultVO;

import java.util.List;

/**
 * 图片识别统一输出。
 */
public record PhotoAiRecognitionResult(
        /** 候选识别结果 */
        List<PhotoAiRecognitionCandidate> candidates,
        /** 结构化识别结果 */
        PhotoStructuredResultVO structuredResult
) {

    public PhotoAiRecognitionResult {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
