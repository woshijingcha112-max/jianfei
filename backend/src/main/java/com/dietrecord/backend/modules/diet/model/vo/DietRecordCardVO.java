package com.dietrecord.backend.modules.diet.model.vo;

public record DietRecordCardVO(
        /** 记录主键 */
        Long id,
        /** 保存时间文案 */
        String savedAt,
        /** 摘要文案 */
        String summary,
        /** 总热量 */
        Integer totalCalories,
        /** 主标签颜色 */
        Integer dominantTag
) {
}
