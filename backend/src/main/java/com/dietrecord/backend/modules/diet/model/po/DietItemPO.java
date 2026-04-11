package com.dietrecord.backend.modules.diet.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@TableName("diet_item")
public class DietItemPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long foodId;

    private String foodName;

    private BigDecimal weightG;

    private BigDecimal calories;

    private Integer tagColor;

    private Integer isConfirmed;
}
