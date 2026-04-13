package com.dietrecord.backend.modules.photo.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PhotoRecognitionItemVO {

    /** 临时条目标识 */
    private final String tempId;

    /** 食物库主键 */
    private final Long foodId;

    /** 食物名称 */
    private final String foodName;

    /** 估算热量 */
    private final BigDecimal calories;

    /** 标签颜色等级 */
    private final Integer tagColor;

    /** 是否命中食物库 */
    private final Boolean matched;

    /** 识别置信度 */
    private final BigDecimal confidence;

    /** 估算重量克数 */
    private final BigDecimal weightG;

    /** 是否已人工确认 */
    private final Boolean isConfirmed;

    public PhotoRecognitionItemVO(String tempId, Long foodId, String foodName, BigDecimal calories, Integer tagColor,
                                  Boolean matched, BigDecimal confidence, BigDecimal weightG, Boolean isConfirmed) {
        this.tempId = tempId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.calories = calories;
        this.tagColor = tagColor;
        this.matched = matched;
        this.confidence = confidence;
        this.weightG = weightG;
        this.isConfirmed = isConfirmed;
    }

}
