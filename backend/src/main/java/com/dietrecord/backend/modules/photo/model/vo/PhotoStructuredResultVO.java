package com.dietrecord.backend.modules.photo.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 多模态结构化识别结果。
 */
public record PhotoStructuredResultVO(
        /** 根识别载荷 */
        @JsonProperty("图片识别结果")
        RecognitionPayload recognitionPayload
) {

    public record RecognitionPayload(
            /** 请求编号 */
            @JsonProperty("请求编号")
            String requestId,
            /** 图片地址 */
            @JsonProperty("图片地址")
            String photoUrl,
            /** 识别时间 */
            @JsonProperty("识别时间")
            String recognizedAt,
            /** 用餐类型 */
            @JsonProperty("用餐类型")
            String mealType,
            /** 记录日期 */
            @JsonProperty("记录日期")
            String recordDate,
            /** 整道菜信息 */
            @JsonProperty("整道菜信息")
            WholeDishInfo wholeDishInfo,
            /** 食材明细 */
            @JsonProperty("食材明细")
            List<IngredientDetail> ingredientDetails,
            /** 汇总信息 */
            @JsonProperty("汇总信息")
            SummaryInfo summaryInfo,
            /** 校验信息 */
            @JsonProperty("校验信息")
            ValidationInfo validationInfo
    ) {
    }

    public record WholeDishInfo(
            /** 菜品名称 */
            @JsonProperty("菜品名称")
            String dishName,
            /** 菜品置信度 */
            @JsonProperty("菜品置信度")
            String dishConfidence,
            /** 菜品分类 */
            @JsonProperty("菜品分类")
            String dishCategory,
            /** 场景描述 */
            @JsonProperty("场景描述")
            String sceneDescription
    ) {
    }

    public record IngredientDetail(
            /** 食材名称 */
            @JsonProperty("食材名称")
            String foodName,
            /** 识别置信度 */
            @JsonProperty("识别置信度")
            String confidence,
            /** 估算重量克数 */
            @JsonProperty("估算重量克")
            String weightG,
            /** 估算热量 */
            @JsonProperty("估算热量")
            String calories,
            /** 食材分类 */
            @JsonProperty("食材分类")
            String category,
            /** 烹饪状态 */
            @JsonProperty("烹饪状态")
            String cookingState,
            /** 数据来源 */
            @JsonProperty("来源")
            String source
    ) {
    }

    public record SummaryInfo(
            /** 总估算热量 */
            @JsonProperty("总估算热量")
            String totalCalories,
            /** 是否需要人工确认 */
            @JsonProperty("是否需要人工确认")
            String needManualConfirm,
            /** 需要人工确认原因 */
            @JsonProperty("需要人工确认原因")
            String manualConfirmReason
    ) {
    }

    public record ValidationInfo(
            /** 百度校验菜名 */
            @JsonProperty("百度校验菜名")
            String baiduDishName,
            /** 百度校验置信度 */
            @JsonProperty("百度校验置信度")
            String baiduDishConfidence,
            /** 百度返回参考热量 */
            @JsonProperty("百度返回参考热量")
            String baiduReferenceCalories,
            /** 校验结论 */
            @JsonProperty("校验结论")
            String validationConclusion
    ) {
    }
}
