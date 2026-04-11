package com.dietrecord.app.core.network.model

import java.math.BigDecimal

data class DietDateDTO(
    val date: String
)

data class GoalSaveDTO(
    val targetWeight: BigDecimal,
    val dailyCalLimit: Int
)

data class DietRecordItemDTO(
    val foodId: Long? = null,
    val foodName: String,
    val weightG: BigDecimal? = null,
    val calories: BigDecimal,
    val tagColor: Int,
    val isConfirmed: Int? = null
)

data class DietRecordSaveDTO(
    val recordDate: String,
    val mealType: Int,
    val photoUrl: String,
    val remark: String? = null,
    val items: List<DietRecordItemDTO>
)

data class GoalGetVO(
    val currentWeightKg: BigDecimal,
    val targetWeightKg: BigDecimal,
    val dailyCalorieLimit: Int
)

data class PhotoRecognitionItemVO(
    val tempId: String,
    val foodId: Long? = null,
    val foodName: String,
    val calories: BigDecimal,
    val tagColor: Int,
    val matched: Boolean,
    val confidence: BigDecimal,
    val weightG: BigDecimal? = null,
    val isConfirmed: Boolean
)

data class PhotoUploadVO(
    val photoUrl: String,
    val recognizedItems: List<PhotoRecognitionItemVO>
)

data class DietRecordCardVO(
    val id: Long,
    val savedAt: String,
    val summary: String,
    val totalCalories: Int,
    val dominantTag: Int
)

data class TodayDietStatVO(
    val dateLabel: String,
    val consumedCalories: Int,
    val targetCalories: Int
)

typealias DateRequest = DietDateDTO
typealias GoalSaveRequest = GoalSaveDTO
typealias DietRecordSaveRequest = DietRecordSaveDTO
typealias DietRecordCardResponse = DietRecordCardVO
typealias GoalGetResponse = GoalGetVO
typealias PhotoUploadResponse = PhotoUploadVO
typealias TodayDietStatResponse = TodayDietStatVO
