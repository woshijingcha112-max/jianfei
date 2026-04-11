package com.dietrecord.app.core.model

enum class FoodTagLevel(
    val label: String
) {
    Light("轻负担"),
    Balanced("均衡"),
    High("高热量")
}

data class GoalUiModel(
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val dailyCalorieLimit: Int
)

data class RecognizedFoodUiModel(
    val id: Long,
    val name: String,
    val calories: Int,
    val tagLevel: FoodTagLevel
)

data class DietRecordCardUiModel(
    val id: Long,
    val savedAt: String,
    val summary: String,
    val totalCalories: Int,
    val dominantTag: FoodTagLevel
)

data class HomeSummaryUiModel(
    val dateLabel: String,
    val consumedCalories: Int,
    val targetCalories: Int,
    val records: List<DietRecordCardUiModel>
)
