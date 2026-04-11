package com.dietrecord.backend.modules.weight.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.common.dto.WeightRecordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weight")
public class WeightController {

    @PostMapping("/record")
    public ApiResponse<Void> record(@Valid @RequestBody WeightRecordRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Weight record scaffold created for date " + request.recordDate());
    }
}
