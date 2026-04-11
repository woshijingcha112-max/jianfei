package com.dietrecord.backend.modules.diet.api;

import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.diet.model.dto.TodayDietStatDTO;
import com.dietrecord.backend.modules.diet.model.vo.TodayDietStatVO;
import com.dietrecord.backend.modules.diet.service.DietStatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 饮食统计相关接口。
 */
@RestController
@RequestMapping("/diet/stat")
@RequiredArgsConstructor
public class DietStatController {

    private final DietStatService dietStatService;

    /**
     * 查询指定日期的当日饮食统计。
     *
     * @param request 当日统计查询请求
     * @return 当日饮食统计结果
     */
    @PostMapping("/today")
    public ApiResponse<TodayDietStatVO> today(@Valid @RequestBody TodayDietStatDTO request) {
        return ApiResponse.success(dietStatService.today(request));
    }
}
