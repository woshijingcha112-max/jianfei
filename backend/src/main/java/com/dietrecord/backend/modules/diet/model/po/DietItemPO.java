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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 饮食记录主键 */
    private Long recordId;

    /** 食物库主键 */
    private Long foodId;

    /** 食物名称 */
    private String foodName;

    /** 估算重量克数 */
    private BigDecimal weightG;

    /** 估算热量 */
    private BigDecimal calories;

    /** 标签颜色等级 */
    private Integer tagColor;

    /** 是否人工确认 */
    private Integer isConfirmed;
}
