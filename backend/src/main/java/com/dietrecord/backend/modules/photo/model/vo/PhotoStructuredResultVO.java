package com.dietrecord.backend.modules.photo.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 多模态结构化识别结果。
 */
public record PhotoStructuredResultVO(
        @JsonProperty("图片识别结果")
        RecognitionPayload recognitionPayload
) {

    public record RecognitionPayload(
            @JsonProperty("请求编号")
            String requestId,
            @JsonProperty("图片地址")
            String photoUrl,
            @JsonProperty("识别时间")
            String recognizedAt,
            @JsonProperty("用餐类型")
            String mealType,
            @JsonProperty("记录日期")
            String recordDate,
            @JsonProperty("整道菜信息")
            WholeDishInfo wholeDishInfo,
            @JsonProperty("食材明细")
            List<IngredientDetail> ingredientDetails,
            @JsonProperty("汇总信息")
            SummaryInfo summaryInfo,
            @JsonProperty("校验信息")
            ValidationInfo validationInfo
    ) {
    }

    public record WholeDishInfo(
            @JsonProperty("菜品名称")
            String dishName,
            @JsonProperty("菜品置信度")
            String dishConfidence,
            @JsonProperty("菜品分类")
            String dishCategory,
            @JsonProperty("场景描述")
            String sceneDescription
    ) {
    }

    public record IngredientDetail(
            @JsonProperty("食材名称")
            String foodName,
            @JsonProperty("识别置信度")
            String confidence,
            @JsonProperty("估算重量克")
            String weightG,
            @JsonProperty("估算热量")
            String calories,
            @JsonProperty("食材分类")
            String category,
            @JsonProperty("烹饪状态")
            String cookingState,
            @JsonProperty("来源")
            String source
    ) {
    }

    public record SummaryInfo(
            @JsonProperty("总估算热量")
            String totalCalories,
            @JsonProperty("是否需要人工确认")
            String needManualConfirm,
            @JsonProperty("需要人工确认原因")
            String manualConfirmReason
    ) {
    }

    public record ValidationInfo(
            @JsonProperty("百度校验菜名")
            String baiduDishName,
            @JsonProperty("百度校验置信度")
            String baiduDishConfidence,
            @JsonProperty("百度返回参考热量")
            String baiduReferenceCalories,
            @JsonProperty("校验结论")
            String validationConclusion
    ) {
    }
}
