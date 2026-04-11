package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoUploadVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PhotoService {

    private final PhotoStorageService photoStorageService;
    private final PhotoRecognitionService photoRecognitionService;

    public PhotoService(PhotoStorageService photoStorageService, PhotoRecognitionService photoRecognitionService) {
        this.photoStorageService = photoStorageService;
        this.photoRecognitionService = photoRecognitionService;
    }

    public PhotoUploadVO uploadAndRecognize(MultipartFile file) {
        String photoUrl = photoStorageService.store(file);
        List<PhotoRecognitionItemVO> recognizedItems = photoRecognitionService.recognize(file.getOriginalFilename());
        return new PhotoUploadVO(photoUrl, recognizedItems);
    }
}
