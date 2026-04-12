package com.dietrecord.backend.modules.photo.model.vo;

import java.util.List;

public class PhotoUploadVO {

    private final String photoUrl;
    private final List<PhotoRecognitionItemVO> recognizedItems;
    private final PhotoStructuredResultVO structuredResult;

    public PhotoUploadVO(String photoUrl,
                         List<PhotoRecognitionItemVO> recognizedItems,
                         PhotoStructuredResultVO structuredResult) {
        this.photoUrl = photoUrl;
        this.recognizedItems = recognizedItems;
        this.structuredResult = structuredResult;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<PhotoRecognitionItemVO> getRecognizedItems() {
        return recognizedItems;
    }

    public PhotoStructuredResultVO getStructuredResult() {
        return structuredResult;
    }
}
