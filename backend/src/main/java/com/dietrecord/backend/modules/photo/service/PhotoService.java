package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;
import com.dietrecord.backend.modules.photo.model.vo.PhotoRecognitionItemVO;
import com.dietrecord.backend.modules.photo.model.vo.PhotoUploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 拍照识别主流程服务。
 */
@Service
@Slf4j
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
        log.info("开始执行拍照识别主流程，原始文件名={}", file.getOriginalFilename());

        // 先处理图片尺寸和格式，确保后续落盘与识别使用统一输入。
        ProcessedPhoto processedPhoto = photoImageProcessingService.process(file);

        // 再保存处理后的图片，保证识别结果页面有稳定的图片访问地址。
        String photoUrl = photoStorageService.store(processedPhoto);

        // 最后执行识别并返回结果页所需数据。
        List<PhotoRecognitionItemVO> recognizedItems = photoRecognitionService.recognize(processedPhoto);

        log.info("拍照识别主流程完成，文件名={}，压缩后大小={}字节，识别结果数={}",
                processedPhoto.originalFilename(), processedPhoto.sizeBytes(), recognizedItems.size());
        return new PhotoUploadVO(photoUrl, recognizedItems);
    }
}
