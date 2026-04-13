package com.dietrecord.backend.modules.system.model.vo;

public record SystemPingVO(
        /** 服务名称 */
        String service,
        /** 服务状态 */
        String status
) {
}
