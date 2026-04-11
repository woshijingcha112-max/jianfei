package com.dietrecord.backend.modules.photo.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/diet/photo")
public class PhotoController {

    @PostMapping("/upload")
    public ApiResponse<Void> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Photo upload scaffold is ready. Current file: " + file.getOriginalFilename());
    }
}
