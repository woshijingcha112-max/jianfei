package com.dietrecord.backend.modules.diet.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.common.dto.DateRequest;
import com.dietrecord.backend.common.dto.DietRecordSaveRequest;
import com.dietrecord.backend.common.dto.RecordIdRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diet/record")
public class DietRecordController {

    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody DietRecordSaveRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Diet record save scaffold created for date " + request.recordDate());
    }

    @PostMapping("/list")
    public ApiResponse<Void> list(@Valid @RequestBody DateRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Diet record list scaffold created for date " + request.date());
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody RecordIdRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Diet record delete scaffold created for recordId " + request.recordId());
    }
}
