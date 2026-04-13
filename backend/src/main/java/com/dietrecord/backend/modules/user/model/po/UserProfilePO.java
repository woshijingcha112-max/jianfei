package com.dietrecord.backend.modules.user.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("user_profile")
public class UserProfilePO {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户昵称 */
    private String nickname;

    /** 身高厘米数 */
    private BigDecimal heightCm;

    /** 当前体重 */
    private BigDecimal weightKg;

    /** 目标体重 */
    private BigDecimal targetWeight;

    /** 每日热量上限 */
    private Integer dailyCalLimit;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
