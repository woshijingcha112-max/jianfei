package com.dietrecord.backend.modules.system.api;

import com.dietrecord.backend.common.api.ApiResponse;
import com.dietrecord.backend.modules.system.model.vo.SystemPingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统基础健康检查接口。
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    /**
     * 返回服务存活状态。
     *
     * @return 系统健康状态
     */
    @GetMapping("/ping")
    public ApiResponse<SystemPingVO> ping() {
        return ApiResponse.success(new SystemPingVO(
                "diet-record-backend",
                "up"
        ));
    }
}
