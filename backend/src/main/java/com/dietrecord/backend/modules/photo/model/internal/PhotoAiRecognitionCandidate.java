package com.dietrecord.backend.modules.photo.model.internal;

import java.math.BigDecimal;

public record PhotoAiRecognitionCandidate(
        /** 食物名称 */
        String foodName,
        /** 识别置信度 */
        BigDecimal confidence,
        /** 估算重量克数 */
        BigDecimal weightG,
        /** 估算热量 */
        BigDecimal calories,
        /** 标签颜色等级 */
        Integer tagColor
) {
}
