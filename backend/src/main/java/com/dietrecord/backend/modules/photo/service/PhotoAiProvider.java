package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.internal.PhotoAiProviderResult;
import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionContext;

/**
 * 图片识别平台适配接口。
 */
public interface PhotoAiProvider {

    /**
     * 当前适配器对应的平台编码。
     *
     * @return 平台编码
     */
    String providerCode();

    /**
     * 当前适配器是否具备可调用条件。
     *
     * @return 是否可调用
     */
    boolean isAvailable();

    /**
     * 执行一次识别请求。
     *
     * @param context 识别上下文
     * @return 平台识别结果
     */
    PhotoAiProviderResult recognize(PhotoAiRecognitionContext context) throws Exception;
}
