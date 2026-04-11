package com.dietrecord.backend.modules.diet.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("diet_record")
public class DietRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    private Integer mealType;

    private String photoUrl;

    private BigDecimal totalCalories;

    private String remark;

    private LocalDateTime createdAt;
}
