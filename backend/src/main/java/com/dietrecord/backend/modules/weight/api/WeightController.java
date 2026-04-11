package com.dietrecord.backend.modules.weight.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.weight.model.dto.WeightRecordDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 体重记录相关接口。
 */
@RestController
@RequestMapping("/weight")
public class WeightController {

    /**
     * 记录体重。
     *
     * @param request 体重记录请求
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/record")
    public ApiResponse<Void> record(@Valid @RequestBody WeightRecordDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Weight record scaffold created for date " + request.recordDate());
    }
}
