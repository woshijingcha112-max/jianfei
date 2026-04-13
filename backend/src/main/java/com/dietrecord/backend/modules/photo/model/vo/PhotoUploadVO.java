package com.dietrecord.backend.modules.photo.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PhotoUploadVO {

    /** 图片访问地址 */
    private final String photoUrl;

    /** 扁平识别结果列表 */
    private final List<PhotoRecognitionItemVO> recognizedItems;

    /** 结构化识别结果 */
    private final PhotoStructuredResultVO structuredResult;

    public PhotoUploadVO(String photoUrl,
                         List<PhotoRecognitionItemVO> recognizedItems,
                         PhotoStructuredResultVO structuredResult) {
        this.photoUrl = photoUrl;
        this.recognizedItems = recognizedItems;
        this.structuredResult = structuredResult;
    }

}
