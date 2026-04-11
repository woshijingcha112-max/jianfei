package com.dietrecord.backend.modules.period.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.common.dto.PeriodRecordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/period")
public class PeriodController {

    @PostMapping("/record")
    public ApiResponse<Void> record(@Valid @RequestBody PeriodRecordRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Period record scaffold created for startDate " + request.startDate());
    }

    @PostMapping("/list")
    public ApiResponse<Void> list() {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED, "Period list scaffold created");
    }
}
