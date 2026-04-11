package com.dietrecord.backend.modules.food.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("food_library")
public class FoodLibraryPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String foodName;

    private String foodNameEn;

    private String alias;

    private String category;

    private BigDecimal caloriesKcal;

    private Integer tagColor;

    private String tagReason;

    private String dataSource;

    private LocalDateTime createdAt;
}
