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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食物中文名 */
    private String foodName;

    /** 食物英文名 */
    private String foodNameEn;

    /** 食物别名 */
    private String alias;

    /** 食物分类 */
    private String category;

    /** 每百克热量 */
    private BigDecimal caloriesKcal;

    /** 标签颜色等级 */
    private Integer tagColor;

    /** 标签提示文案 */
    private String tagReason;

    /** 数据来源 */
    private String dataSource;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
