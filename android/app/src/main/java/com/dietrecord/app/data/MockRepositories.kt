package com.dietrecord.app.data

import java.io.File
import com.dietrecord.app.core.data.AppDispatchers
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.FoodTagLevel
import com.dietrecord.app.core.model.GoalUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.core.network.DietApiService
import com.dietrecord.app.core.network.model.ApiEnvelope
import com.dietrecord.app.core.network.model.DietDateDTO
import com.dietrecord.app.core.network.model.DietRecordSaveDTO
import com.dietrecord.app.core.network.model.GoalSaveDTO
import com.dietrecord.app.core.network.model.PhotoUploadVO
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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
    val currentRecognitionSessionFlow: StateFlow<RecognitionSessionState>

    suspend fun recognizeCapturedPhoto(photoFile: File): List<RecognizedFoodUiModel>

    suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel>

    suspend fun clearCurrentRecognition()
}

class RealGoalRepository(
    private val api: DietApiService,
    private val scope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : GoalRepository {
    private val _goalFlow = MutableStateFlow(defaultGoal())
    override val goalFlow: StateFlow<GoalUiModel> = _goalFlow.asStateFlow()

    init {
        scope.launch {
            runCatching { refreshGoal() }
        }
    }

    override suspend fun getGoal(): GoalUiModel = withContext(dispatchers.io) {
        refreshGoal()
    }

    override suspend fun saveGoal(goal: GoalUiModel) {
        withContext(dispatchers.io) {
            api.saveGoal(goal.toSaveDTO()).ensureSuccess()
            refreshGoal()
        }
    }

    private suspend fun refreshGoal(): GoalUiModel {
        val goal = api.getGoal().requireData().toUiModel()
        _goalFlow.value = goal
        return goal
    }
}

class RealDietRecordRepository(
    private val api: DietApiService,
    private val recognitionRepository: RecognitionRepository,
    private val scope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : DietRecordRepository {
    private val _todayRecordsFlow = MutableStateFlow<List<DietRecordCardUiModel>>(emptyList())
    override val todayRecordsFlow: StateFlow<List<DietRecordCardUiModel>> = _todayRecordsFlow.asStateFlow()

    init {
        scope.launch {
            runCatching { refreshTodayRecords(LocalDate.now()) }
        }
    }

    override suspend fun getTodaySummary(date: LocalDate): HomeSummaryUiModel = withContext(dispatchers.io) {
        val stat = api.todayStat(DietDateDTO(date.toString())).requireData()
        HomeSummaryUiModel(
            dateLabel = stat.dateLabel,
            consumedCalories = stat.consumedCalories,
            targetCalories = stat.targetCalories,
            records = todayRecordsFlow.value
        )
    }

    override suspend fun listTodayRecords(date: LocalDate): List<DietRecordCardUiModel> = withContext(dispatchers.io) {
        refreshTodayRecords(date)
    }

    override suspend fun saveRecognizedRecord(
        items: List<RecognizedFoodUiModel>,
        timestamp: LocalDateTime
    ) {
        withContext(dispatchers.io) {
            val session = recognitionRepository.currentRecognitionSessionFlow.value
            if (session.photoUrl.isBlank()) {
                throw IllegalStateException("photoUrl is required before saving diet record")
            }

            val saveRequest = DietRecordSaveDTO(
                recordDate = timestamp.toLocalDate().toString(),
                mealType = 2,
                photoUrl = session.photoUrl,
                items = session.toSaveRequestItems().ifEmpty { items.toSaveRequestItemsFallback() }
            )

            api.saveDietRecord(saveRequest).ensureSuccess()
            refreshTodayRecords(timestamp.toLocalDate())
        }
    }

    private suspend fun refreshTodayRecords(date: LocalDate): List<DietRecordCardUiModel> {
        val records = api.listDietRecords(DietDateDTO(date.toString())).requireData().map { it.toUiModel() }
        if (date == LocalDate.now()) {
            _todayRecordsFlow.value = records
        }
        return records
    }
}

class RealRecognitionRepository(
    private val api: DietApiService,
    private val scope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : RecognitionRepository {
    private val _sessionFlow = MutableStateFlow(RecognitionSessionState())
    override val currentRecognitionSessionFlow: StateFlow<RecognitionSessionState> = _sessionFlow.asStateFlow()
    override val currentRecognitionFlow: StateFlow<List<RecognizedFoodUiModel>> = _sessionFlow
        .map { it.toUiModels() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun recognizeCapturedPhoto(photoFile: File): List<RecognizedFoodUiModel> = withContext(dispatchers.io) {
        val response = api.uploadPhoto(photoFile.toMultipartBodyPart()).requireData()
        val session = response.toSessionState()
        _sessionFlow.value = session
        session.toUiModels()
    }

    override suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel> = withContext(dispatchers.io) {
        currentRecognitionFlow.value
    }

    override suspend fun clearCurrentRecognition() {
        withContext(dispatchers.io) {
            _sessionFlow.value = RecognitionSessionState()
        }
    }

    private fun File.toMultipartBodyPart(): MultipartBody.Part {
        val mimeType = when (extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }.toMediaType()
        val requestBody = asRequestBody(mimeType)
        return MultipartBody.Part.createFormData("file", name, requestBody)
    }

    private fun PhotoUploadVO.toSessionState(): RecognitionSessionState {
        return RecognitionSessionState(
            photoUrl = photoUrl,
            recognizedItems = recognizedItems.map { it.toSessionItem() }
        )
    }
}

private fun defaultGoal(): GoalUiModel {
    return GoalUiModel(
        currentWeightKg = 70.0,
        targetWeightKg = 65.0,
        dailyCalorieLimit = 1500
    )
}

private fun GoalUiModel.toSaveDTO(): GoalSaveDTO {
    return GoalSaveDTO(
        targetWeight = BigDecimal.valueOf(targetWeightKg),
        dailyCalLimit = dailyCalorieLimit
    )
}

private fun com.dietrecord.app.core.network.model.GoalGetVO.toUiModel(): GoalUiModel {
    return GoalUiModel(
        currentWeightKg = currentWeightKg.toDouble(),
        targetWeightKg = targetWeightKg.toDouble(),
        dailyCalorieLimit = dailyCalorieLimit
    )
}

private fun ApiEnvelope<*>.ensureSuccess() {
    if (code != 0) {
        throw IllegalStateException(msg.ifBlank { "request failed" })
    }
}

private fun <T> ApiEnvelope<T>.requireData(): T {
    ensureSuccess()
    return data ?: throw IllegalStateException(msg.ifBlank { "empty response data" })
}

private fun com.dietrecord.app.core.network.model.DietRecordCardVO.toUiModel(): DietRecordCardUiModel {
    return DietRecordCardUiModel(
        id = id,
        savedAt = savedAt,
        summary = summary,
        totalCalories = totalCalories,
        dominantTag = dominantTag.toFoodTagLevel()
    )
}

private fun Int.toFoodTagLevel(): com.dietrecord.app.core.model.FoodTagLevel {
    return when (this) {
        1 -> com.dietrecord.app.core.model.FoodTagLevel.Light
        3 -> com.dietrecord.app.core.model.FoodTagLevel.High
        else -> com.dietrecord.app.core.model.FoodTagLevel.Balanced
    }
}

private fun List<RecognizedFoodUiModel>.toSaveRequestItemsFallback(): List<com.dietrecord.app.core.network.model.DietRecordItemDTO> {
    return map { item ->
        com.dietrecord.app.core.network.model.DietRecordItemDTO(
            foodId = item.id,
            foodName = item.name,
            weightG = null,
            calories = BigDecimal.valueOf(item.calories.toLong()),
            tagColor = item.tagLevel.toTagColor(),
            isConfirmed = 1
        )
    }
}

private fun FoodTagLevel.toTagColor(): Int {
    return when (this) {
        FoodTagLevel.Light -> 1
        FoodTagLevel.High -> 3
        FoodTagLevel.Balanced -> 2
    }
}
