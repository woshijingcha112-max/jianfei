package com.dietrecord.backend.modules.diet.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.common.dto.DateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diet/stat")
public class DietStatController {

    @PostMapping("/today")
    public ApiResponse<Void> today(@Valid @RequestBody DateRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Diet stat scaffold created for date " + request.date());
    }
}
