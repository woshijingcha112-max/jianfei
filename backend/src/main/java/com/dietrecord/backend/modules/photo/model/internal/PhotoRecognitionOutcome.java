package com.dietrecord.backend.modules.photo.model.internal;

import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoStructuredResultVO;

import java.util.List;

/**
 * 识别结果页输出模型。
 */
public record PhotoRecognitionOutcome(
        /** 扁平识别结果列表 */
        List<PhotoRecognitionItemVO> recognizedItems,
        /** 结构化识别结果 */
        PhotoStructuredResultVO structuredResult
) {

    public PhotoRecognitionOutcome {
        recognizedItems = recognizedItems == null ? List.of() : List.copyOf(recognizedItems);
    }
}
