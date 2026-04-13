package com.dietrecord.backend.modules.photo.model.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图片识别上下文。
 */
public record PhotoAiRecognitionContext(
        /** 请求编号 */
        String requestId,
        /** 处理后图片 */
        ProcessedPhoto processedPhoto,
        /** 图片访问地址 */
        String photoUrl,
        /** 用餐类型 */
        String mealType,
        /** 记录日期 */
        LocalDate recordDate,
        /** 识别时间 */
        LocalDateTime recognizedAt
) {
}
