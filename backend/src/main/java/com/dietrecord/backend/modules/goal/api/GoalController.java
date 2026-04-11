package com.dietrecord.backend.modules.goal.api;

import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.goal.model.dto.GoalSaveDTO;
import com.dietrecord.backend.modules.goal.model.vo.GoalGetVO;
import com.dietrecord.backend.modules.goal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目标设置相关接口。
 */
@RestController
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * 读取当前用户目标设置。
     *
     * @return 当前目标设置
     */
    @PostMapping("/get")
    public ApiResponse<GoalGetVO> get() {
        return ApiResponse.success(goalService.getGoal());
    }

    /**
     * 保存当前用户目标设置。
     *
     * @param request 目标保存请求
     * @return 保存结果
     */
    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody GoalSaveDTO request) {
        goalService.saveGoal(request);
        return ApiResponse.<Void>success(null);
    }
}
