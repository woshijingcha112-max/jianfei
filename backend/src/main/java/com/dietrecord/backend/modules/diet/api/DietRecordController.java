package com.dietrecord.backend.modules.diet.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordDeleteDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordListDTO;
import com.dietrecord.backend.modules.diet.model.dto.DietRecordSaveDTO;
import com.dietrecord.backend.modules.diet.model.vo.DietRecordCardVO;
import com.dietrecord.backend.modules.diet.service.DietRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 饮食记录相关接口。
 */
@RestController
@RequestMapping("/diet/record")
@RequiredArgsConstructor
public class DietRecordController {

    private final DietRecordService dietRecordService;

    /**
     * 保存饮食记录。
     *
     * @param request 饮食记录保存请求
     * @return 保存结果
     */
    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody DietRecordSaveDTO request) {
        dietRecordService.save(request);
        return ApiResponse.success(null);
    }

    /**
     * 查询指定日期的饮食记录列表。
     *
     * @param request 饮食记录列表查询请求
     * @return 记录卡片列表
     */
    @PostMapping("/list")
    public ApiResponse<List<DietRecordCardVO>> list(@Valid @RequestBody DietRecordListDTO request) {
        return ApiResponse.success(dietRecordService.list(request));
    }

    /**
     * 删除饮食记录。
     *
     * @param request 饮食记录删除请求
     * @return 当前轮次固定返回未实现
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody DietRecordDeleteDTO request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Diet record delete scaffold created for recordId " + request.recordId());
    }
}
