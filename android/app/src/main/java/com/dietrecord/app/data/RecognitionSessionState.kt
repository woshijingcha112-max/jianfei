package com.dietrecord.app.data

import com.dietrecord.app.core.model.FoodTagLevel
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.core.network.model.DietRecordItemDTO
import com.dietrecord.app.core.network.model.PhotoRecognitionItemVO
import java.math.BigDecimal
import java.math.RoundingMode

data class RecognitionItemState(
    val tempId: String,
    val foodId: Long? = null,
    val foodName: String,
    val calories: BigDecimal,
    val tagColor: Int,
    val matched: Boolean,
    val confidence: BigDecimal,
    val weightG: BigDecimal? = null,
    val isConfirmed: Boolean = false
) {
    fun toUiModel(id: Long): RecognizedFoodUiModel {
        return RecognizedFoodUiModel(
            id = id,
            name = foodName,
            calories = calories.toCaloriesInt(),
            tagLevel = tagColor.toFoodTagLevel()
        )
    }

    fun toSaveRequest(): DietRecordItemDTO {
        return DietRecordItemDTO(
            foodId = foodId,
            foodName = foodName,
            weightG = weightG,
            calories = calories,
            tagColor = tagColor,
            isConfirmed = if (isConfirmed) 1 else 0
        )
    }
}

data class RecognitionSessionState(
    val photoUrl: String = "",
    val recognizedItems: List<RecognitionItemState> = emptyList()
) {
    fun toUiModels(): List<RecognizedFoodUiModel> {
        return recognizedItems.mapIndexed { index, item ->
            item.toUiModel((index + 1).toLong())
        }
    }

    fun toSaveRequestItems(): List<DietRecordItemDTO> {
        return recognizedItems.map { it.toSaveRequest() }
    }
}

fun PhotoRecognitionItemVO.toSessionItem(): RecognitionItemState {
    return RecognitionItemState(
        tempId = tempId,
        foodId = foodId,
        foodName = foodName,
        calories = calories,
        tagColor = tagColor,
        matched = matched,
        confidence = confidence,
        weightG = weightG,
        isConfirmed = isConfirmed
    )
}

private fun BigDecimal.toCaloriesInt(): Int {
    return setScale(0, RoundingMode.HALF_UP).toInt()
}

private fun Int.toFoodTagLevel(): FoodTagLevel {
    return when (this) {
        1 -> FoodTagLevel.Light
        3 -> FoodTagLevel.High
        else -> FoodTagLevel.Balanced
    }
}
