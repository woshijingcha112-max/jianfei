package com.dietrecord.app.data

import com.dietrecord.app.core.data.AppDispatchers
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.FoodTagLevel
import com.dietrecord.app.core.model.GoalUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface GoalRepository {
    val goalFlow: StateFlow<GoalUiModel>

    suspend fun getGoal(): GoalUiModel

    suspend fun saveGoal(goal: GoalUiModel)
}

interface DietRecordRepository {
    val todayRecordsFlow: StateFlow<List<DietRecordCardUiModel>>

    suspend fun getTodaySummary(date: LocalDate = LocalDate.now()): HomeSummaryUiModel

    suspend fun listTodayRecords(date: LocalDate = LocalDate.now()): List<DietRecordCardUiModel>

    suspend fun saveRecognizedRecord(
        items: List<RecognizedFoodUiModel>,
        timestamp: LocalDateTime = LocalDateTime.now()
    )
}

interface RecognitionRepository {
    val currentRecognitionFlow: StateFlow<List<RecognizedFoodUiModel>>

    suspend fun recognizeSamplePhoto(): List<RecognizedFoodUiModel>

    suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel>

    suspend fun clearCurrentRecognition()
}

class MockSessionStore {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _goalFlow = MutableStateFlow(
        GoalUiModel(
            currentWeightKg = 70.0,
            targetWeightKg = 65.0,
            dailyCalorieLimit = 1500
        )
    )
    val goalFlow: StateFlow<GoalUiModel> = _goalFlow.asStateFlow()

    private val _todayRecordsFlow = MutableStateFlow<List<DietRecordCardUiModel>>(emptyList())
    val todayRecordsFlow: StateFlow<List<DietRecordCardUiModel>> = _todayRecordsFlow.asStateFlow()

    private val _currentRecognitionFlow = MutableStateFlow<List<RecognizedFoodUiModel>>(emptyList())
    val currentRecognitionFlow: StateFlow<List<RecognizedFoodUiModel>> = _currentRecognitionFlow.asStateFlow()

    private var nextRecordId = 1L

    fun todayLabel(date: LocalDate = LocalDate.now()): String = date.format(dateFormatter)

    fun saveGoal(goal: GoalUiModel) {
        _goalFlow.value = goal
    }

    fun setCurrentRecognition(items: List<RecognizedFoodUiModel>) {
        _currentRecognitionFlow.value = items
    }

    fun clearCurrentRecognition() {
        _currentRecognitionFlow.value = emptyList()
    }

    fun saveRecognizedRecord(
        items: List<RecognizedFoodUiModel>,
        timestamp: LocalDateTime
    ) {
        val summary = items.joinToString(separator = "、") { it.name }
        val totalCalories = items.sumOf { it.calories }
        val dominantTag = items.maxByOrNull { it.calories }?.tagLevel ?: FoodTagLevel.Balanced
        val card = DietRecordCardUiModel(
            id = nextRecordId++,
            savedAt = timestamp.format(timeFormatter),
            summary = summary,
            totalCalories = totalCalories,
            dominantTag = dominantTag
        )
        _todayRecordsFlow.value = listOf(card) + _todayRecordsFlow.value
    }

    fun sampleRecognition(): List<RecognizedFoodUiModel> {
        return listOf(
            RecognizedFoodUiModel(
                id = 1L,
                name = "鸡蛋",
                calories = 144,
                tagLevel = FoodTagLevel.Balanced
            ),
            RecognizedFoodUiModel(
                id = 2L,
                name = "番茄",
                calories = 30,
                tagLevel = FoodTagLevel.Light
            ),
            RecognizedFoodUiModel(
                id = 3L,
                name = "炒饭",
                calories = 280,
                tagLevel = FoodTagLevel.High
            )
        )
    }
}

class MockGoalRepository(
    private val store: MockSessionStore,
    private val dispatchers: AppDispatchers
) : GoalRepository {
    override val goalFlow: StateFlow<GoalUiModel> = store.goalFlow

    override suspend fun getGoal(): GoalUiModel = withContext(dispatchers.io) {
        delay(150)
        store.goalFlow.value
    }

    override suspend fun saveGoal(goal: GoalUiModel) {
        withContext(dispatchers.io) {
            delay(250)
            store.saveGoal(goal)
        }
    }
}

class MockDietRecordRepository(
    private val store: MockSessionStore,
    private val dispatchers: AppDispatchers
) : DietRecordRepository {
    override val todayRecordsFlow: StateFlow<List<DietRecordCardUiModel>> = store.todayRecordsFlow

    override suspend fun getTodaySummary(date: LocalDate): HomeSummaryUiModel = withContext(dispatchers.io) {
        delay(120)
        val records = store.todayRecordsFlow.value
        HomeSummaryUiModel(
            dateLabel = store.todayLabel(date),
            consumedCalories = records.sumOf { it.totalCalories },
            targetCalories = store.goalFlow.value.dailyCalorieLimit,
            records = records
        )
    }

    override suspend fun listTodayRecords(date: LocalDate): List<DietRecordCardUiModel> = withContext(dispatchers.io) {
        delay(80)
        store.todayRecordsFlow.value
    }

    override suspend fun saveRecognizedRecord(
        items: List<RecognizedFoodUiModel>,
        timestamp: LocalDateTime
    ) {
        withContext(dispatchers.io) {
            delay(300)
            store.saveRecognizedRecord(items, timestamp)
        }
    }
}

class MockRecognitionRepository(
    private val store: MockSessionStore,
    private val dispatchers: AppDispatchers
) : RecognitionRepository {
    override val currentRecognitionFlow: StateFlow<List<RecognizedFoodUiModel>> = store.currentRecognitionFlow

    override suspend fun recognizeSamplePhoto(): List<RecognizedFoodUiModel> = withContext(dispatchers.io) {
        delay(350)
        val items = store.sampleRecognition()
        store.setCurrentRecognition(items)
        items
    }

    override suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel> = withContext(dispatchers.io) {
        store.currentRecognitionFlow.value
    }

    override suspend fun clearCurrentRecognition() {
        withContext(dispatchers.io) {
            store.clearCurrentRecognition()
        }
    }
}
