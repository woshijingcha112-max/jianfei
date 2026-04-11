package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoUploadVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PhotoService {

    private final PhotoStorageService photoStorageService;
    private final PhotoImageProcessingService photoImageProcessingService;
    private final PhotoRecognitionService photoRecognitionService;

    public PhotoService(PhotoStorageService photoStorageService,
                        PhotoImageProcessingService photoImageProcessingService,
                        PhotoRecognitionService photoRecognitionService) {
        this.photoStorageService = photoStorageService;
        this.photoImageProcessingService = photoImageProcessingService;
        this.photoRecognitionService = photoRecognitionService;
    }

    public PhotoUploadVO uploadAndRecognize(MultipartFile file) {
        var processedPhoto = photoImageProcessingService.process(file);
        String photoUrl = photoStorageService.store(processedPhoto);
        List<PhotoRecognitionItemVO> recognizedItems = photoRecognitionService.recognize(processedPhoto);
        return new PhotoUploadVO(photoUrl, recognizedItems);
    }
}
