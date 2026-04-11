package com.dietrecord.backend.modules.system.api;

import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.system.model.vo.SystemPingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemController {

    @GetMapping("/ping")
    public ApiResponse<SystemPingVO> ping() {
        return ApiResponse.success(new SystemPingVO(
                "diet-record-backend",
                "up"
        ));
    }
}
