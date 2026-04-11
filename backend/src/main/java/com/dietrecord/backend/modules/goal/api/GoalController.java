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

@RestController
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/get")
    public ApiResponse<GoalGetVO> get() {
        return ApiResponse.success(goalService.getGoal());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody GoalSaveDTO request) {
        goalService.saveGoal(request);
        return ApiResponse.<Void>success(null);
    }
}
