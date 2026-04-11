package com.dietrecord.backend.modules.photo.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.photo.model.vo.PhotoUploadVO;
import com.dietrecord.backend.modules.photo.service.PhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 拍照识别相关接口。
 */
@RestController
@Slf4j
@RequestMapping("/diet/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * 上传餐食图片并返回识别结果。
     *
     * @param file 用户上传的图片文件
     * @return 图片访问地址与识别结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PhotoUploadVO> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail(ApiCode.VALIDATE_ERROR, "file is required");
        }

        try {
            log.info("开始处理拍照上传请求，文件名={}，文件大小={}字节", file.getOriginalFilename(), file.getSize());
            return ApiResponse.success(photoService.uploadAndRecognize(file));
        } catch (IllegalArgumentException ex) {
            log.warn("拍照上传请求参数校验失败，原因={}", ex.getMessage());
            return ApiResponse.fail(ApiCode.VALIDATE_ERROR, ex.getMessage());
        } catch (Exception ex) {
            log.error("拍照上传处理失败，文件名={}", file.getOriginalFilename(), ex);
            return ApiResponse.fail(ApiCode.INTERNAL_ERROR, "photo upload failed: " + ex.getMessage());
        }
    }
}
