package com.dietrecord.backend.modules.photo.model.vo;

import java.util.List;

public class PhotoUploadVO {

    private final String photoUrl;
    private final List<PhotoRecognitionItemVO> recognizedItems;

    public PhotoUploadVO(String photoUrl, List<PhotoRecognitionItemVO> recognizedItems) {
        this.photoUrl = photoUrl;
        this.recognizedItems = recognizedItems;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<PhotoRecognitionItemVO> getRecognizedItems() {
        return recognizedItems;
    }
}
