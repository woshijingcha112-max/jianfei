package com.dietrecord.backend.modules.period.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.period.model.dto.PeriodRecordDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生理期相关接口。
 */
@RestController
@RequestMapping("/period")
public class PeriodController {

    /**
     * 记录生理期。
     *
     * @param request 生理期记录请求
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/record")
    public ApiResponse<Void> record(@Valid @RequestBody PeriodRecordDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Period record scaffold created for startDate " + request.startDate());
    }

    /**
     * 查询生理期记录列表。
     *
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/list")
    public ApiResponse<Void> list() {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED, "Period list scaffold created");
    }
}
