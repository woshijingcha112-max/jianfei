package com.dietrecord.backend.modules.photo.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.photo.model.vo.PhotoUploadVO;
import com.dietrecord.backend.modules.photo.service.PhotoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/diet/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PhotoUploadVO> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail(ApiCode.VALIDATE_ERROR, "file is required");
        }
        try {
            return ApiResponse.success(photoService.uploadAndRecognize(file));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ApiCode.VALIDATE_ERROR, ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.fail(ApiCode.INTERNAL_ERROR, "photo upload failed: " + ex.getMessage());
        }
    }
}
