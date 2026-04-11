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

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private BigDecimal targetWeight;

    private Integer dailyCalLimit;

    private LocalDateTime createdAt;
}
