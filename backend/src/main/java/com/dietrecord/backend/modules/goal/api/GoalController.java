package com.dietrecord.backend.modules.goal.api;

import com.dietrecord.backend.common.api.ApiCode;
import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.common.dto.GoalSaveRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goal")
public class GoalController {

    @PostMapping("/get")
    public ApiResponse<Void> get() {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED, "Goal get scaffold created");
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@RequestBody GoalSaveRequest request) {
        return ApiResponse.fail(ApiCode.NOT_IMPLEMENTED,
                "Goal save scaffold created with dailyCalLimit " + request.dailyCalLimit());
    }
}
