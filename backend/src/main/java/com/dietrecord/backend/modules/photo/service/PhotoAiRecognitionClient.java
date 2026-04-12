package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionResult;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;

/**
 * 图片识别客户端接口。
 */
public interface PhotoAiRecognitionClient {

    /**
     * 对处理后的图片执行识别。
     *
     * @param processedPhoto 已完成格式和尺寸处理的图片
     * @param photoUrl       已落盘后的图片访问地址
     * @return 识别候选结果列表
     */
    PhotoAiRecognitionResult recognize(ProcessedPhoto processedPhoto, String photoUrl);
}
