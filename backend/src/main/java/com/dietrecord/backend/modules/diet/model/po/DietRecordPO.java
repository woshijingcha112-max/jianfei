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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户主键 */
    private Long userId;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 餐次类型 */
    private Integer mealType;

    /** 图片访问地址 */
    private String photoUrl;

    /** 本餐总热量 */
    private BigDecimal totalCalories;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
