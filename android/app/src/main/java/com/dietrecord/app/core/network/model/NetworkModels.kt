package com.dietrecord.app.core.network.model

data class DateRequest(
    val date: String
)

data class DietRecordSaveRequest(
    val recordDate: String,
    val mealType: Int,
    val remark: String? = null
)

data class FoodSearchRequest(
    val keyword: String
)

data class GoalSaveRequest(
    val targetWeight: Double?,
    val dailyCalLimit: Int?
)

data class IdRequest(
    val id: Long
)

data class PeriodRecordRequest(
    val startDate: String,
    val endDate: String? = null,
    val remark: String? = null
)

data class RecordIdRequest(
    val recordId: Long
)

data class WeightRecordRequest(
    val weightKg: Double,
    val recordDate: String
)
