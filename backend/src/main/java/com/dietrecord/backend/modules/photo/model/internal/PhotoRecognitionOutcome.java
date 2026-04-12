package com.dietrecord.backend.modules.photo.model.internal;

import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoStructuredResultVO;

import java.util.List;

/**
 * 识别结果页输出模型。
 */
public record PhotoRecognitionOutcome(
        List<PhotoRecognitionItemVO> recognizedItems,
        PhotoStructuredResultVO structuredResult
) {

    public PhotoRecognitionOutcome {
        recognizedItems = recognizedItems == null ? List.of() : List.copyOf(recognizedItems);
    }
}
