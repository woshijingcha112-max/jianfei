package com.dietrecord.app.data

import android.util.Log
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
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
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

private const val TAG = "Repositories"
private const val PHOTO_UPLOAD_TAG = "PhotoUpload"

/**
 * 目标设置仓储接口。
 *
 * Java 开发可将其理解为“目标模块的数据访问门面”。
 */
interface GoalRepository {
    val goalFlow: StateFlow<GoalUiModel>

    suspend fun getGoal(): GoalUiModel

    suspend fun saveGoal(goal: GoalUiModel)
}

/**
 * 饮食记录仓储接口。
 */
interface DietRecordRepository {
    val todayRecordsFlow: StateFlow<List<DietRecordCardUiModel>>

    suspend fun getTodaySummary(date: LocalDate = LocalDate.now()): HomeSummaryUiModel

    suspend fun listTodayRecords(date: LocalDate = LocalDate.now()): List<DietRecordCardUiModel>

    suspend fun saveRecognizedRecord(
        items: List<RecognizedFoodUiModel>,
        timestamp: LocalDateTime = LocalDateTime.now()
    )
}

/**
 * 拍照识别仓储接口。
 */
interface RecognitionRepository {
    val currentRecognitionFlow: StateFlow<List<RecognizedFoodUiModel>>
    val currentRecognitionSessionFlow: StateFlow<RecognitionSessionState>

    suspend fun recognizeCapturedPhoto(photoFile: File): List<RecognizedFoodUiModel>

    suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel>

    suspend fun clearCurrentRecognition()
}

/**
 * 目标设置真实仓储实现。
 */
class RealGoalRepository(
    private val api: DietApiService,
    private val scope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : GoalRepository {
    private val _goalFlow = MutableStateFlow(defaultGoal())
    override val goalFlow: StateFlow<GoalUiModel> = _goalFlow.asStateFlow()

    init {
        scope.launch {
            Log.d(TAG, "启动时预加载目标设置")
            runCatching { refreshGoal() }
        }
    }

    override suspend fun getGoal(): GoalUiModel = withContext(dispatchers.io) {
        Log.i(TAG, "开始读取目标设置")
        refreshGoal()
    }

    override suspend fun saveGoal(goal: GoalUiModel) {
        withContext(dispatchers.io) {
            Log.i(TAG, "开始保存目标设置，目标体重=${goal.targetWeightKg}，热量上限=${goal.dailyCalorieLimit}")
            val envelope = api.saveGoal(goal.toSaveDTO())
            Log.i(TAG, "目标保存接口返回，code=${envelope.code}，msg=${envelope.msg}")
            envelope.ensureSuccess()
            _goalFlow.value = goal
            Log.i(TAG, "目标设置保存完成")
        }
    }

    private suspend fun refreshGoal(): GoalUiModel {
        val goal = api.getGoal().requireData().toUiModel()
        _goalFlow.value = goal
        Log.d(TAG, "目标设置刷新完成，当前体重=${goal.currentWeightKg}，目标体重=${goal.targetWeightKg}")
        return goal
    }
}

/**
 * 饮食记录真实仓储实现。
 */
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
            Log.d(TAG, "启动时预加载今日饮食记录")
            runCatching { refreshTodayRecords(LocalDate.now()) }
        }
    }

    override suspend fun getTodaySummary(date: LocalDate): HomeSummaryUiModel = withContext(dispatchers.io) {
        Log.i(TAG, "开始读取首页总览，日期=$date")
        val stat = api.todayStat(DietDateDTO(date.toString())).requireData()
        HomeSummaryUiModel(
            dateLabel = stat.dateLabel,
            consumedCalories = stat.consumedCalories,
            targetCalories = stat.targetCalories,
            records = todayRecordsFlow.value
        )
    }

    override suspend fun listTodayRecords(date: LocalDate): List<DietRecordCardUiModel> = withContext(dispatchers.io) {
        Log.i(TAG, "开始读取饮食记录列表，日期=$date")
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

            // 保存记录时优先复用当前识别会话中的 photoUrl 和识别项。
            val saveRequest = DietRecordSaveDTO(
                recordDate = timestamp.toLocalDate().toString(),
                mealType = 2,
                photoUrl = session.photoUrl,
                items = session.toSaveRequestItems().ifEmpty { items.toSaveRequestItemsFallback() }
            )

            Log.i(TAG, "开始保存识别后的饮食记录，日期=${timestamp.toLocalDate()}，条目数=${saveRequest.items.size}")
            api.saveDietRecord(saveRequest).ensureSuccess()
            refreshTodayRecords(timestamp.toLocalDate())
            Log.i(TAG, "识别后的饮食记录保存完成")
        }
    }

    private suspend fun refreshTodayRecords(date: LocalDate): List<DietRecordCardUiModel> {
        val records = api.listDietRecords(DietDateDTO(date.toString())).requireData().map { it.toUiModel() }
        if (date == LocalDate.now()) {
            _todayRecordsFlow.value = records
        }
        Log.d(TAG, "饮食记录刷新完成，日期=$date，记录数=${records.size}")
        return records
    }
}

/**
 * 拍照识别真实仓储实现。
 */
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
        val startNanos = System.nanoTime()
        Log.i(PHOTO_UPLOAD_TAG, "开始上传拍照图片，file=${photoFile.name}，sizeBytes=${photoFile.length()}")

        runCatching {
            val envelope = api.uploadPhoto(photoFile.toMultipartBodyPart())
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
            Log.i(
                PHOTO_UPLOAD_TAG,
                "上传接口返回，code=${envelope.code}，msg=${envelope.msg}，hasData=${envelope.data != null}，elapsedMs=$elapsedMs"
            )

            val response = envelope.requireData()
            val session = response.toSessionState()
            _sessionFlow.value = session
            Log.i(
                PHOTO_UPLOAD_TAG,
                "拍照识别完成，photoUrl=${response.photoUrl}，recognizedItems=${session.recognizedItems.size}，elapsedMs=$elapsedMs"
            )
            session.toUiModels()
        }.onFailure {
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
            Log.e(
                PHOTO_UPLOAD_TAG,
                "拍照识别接口失败，file=${photoFile.name}，elapsedMs=$elapsedMs，error=${it::class.java.simpleName}:${it.message}",
                it
            )
        }.getOrThrow()
    }

    override suspend fun getCurrentRecognition(): List<RecognizedFoodUiModel> = withContext(dispatchers.io) {
        currentRecognitionFlow.value
    }

    override suspend fun clearCurrentRecognition() {
        withContext(dispatchers.io) {
            _sessionFlow.value = RecognitionSessionState()
            Log.d(TAG, "已清空当前识别会话")
        }
    }

    /**
     * 把本地拍照文件包装成 multipart。
     */
    private fun File.toMultipartBodyPart(): MultipartBody.Part {
        val mimeType = when (extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }.toMediaType()
        val requestBody = asRequestBody(mimeType)
        return MultipartBody.Part.createFormData("file", name, requestBody)
    }

    /**
     * 把后端返回结果转换为当前识别会话状态。
     */
    private fun PhotoUploadVO.toSessionState(): RecognitionSessionState {
        return RecognitionSessionState(
            photoUrl = photoUrl,
            recognizedItems = recognizedItems.map { it.toSessionItem() }
        )
    }
}

/**
 * 目标页默认展示模型。
 */
private fun defaultGoal(): GoalUiModel {
    return GoalUiModel(
        currentWeightKg = 70.0,
        targetWeightKg = 65.0,
        dailyCalorieLimit = 1500
    )
}

/**
 * 目标页面模型转换为后端保存请求。
 */
private fun GoalUiModel.toSaveDTO(): GoalSaveDTO {
    return GoalSaveDTO(
        currentWeight = BigDecimal.valueOf(currentWeightKg),
        targetWeight = BigDecimal.valueOf(targetWeightKg),
        dailyCalLimit = dailyCalorieLimit
    )
}

/**
 * 后端目标数据转换为页面模型。
 */
private fun com.dietrecord.app.core.network.model.GoalGetVO.toUiModel(): GoalUiModel {
    return GoalUiModel(
        currentWeightKg = currentWeightKg.toDouble(),
        targetWeightKg = targetWeightKg.toDouble(),
        dailyCalorieLimit = dailyCalorieLimit
    )
}

/**
 * 统一处理后端通用响应码。
 */
private fun ApiEnvelope<*>.ensureSuccess() {
    if (code != 0) {
        throw IllegalStateException(msg.ifBlank { "request failed" })
    }
}

/**
 * 读取通用响应中的 data 载荷。
 */
private fun <T> ApiEnvelope<T>.requireData(): T {
    ensureSuccess()
    return data ?: throw IllegalStateException(msg.ifBlank { "empty response data" })
}

/**
 * 列表项转换为首页卡片模型。
 */
private fun com.dietrecord.app.core.network.model.DietRecordCardVO.toUiModel(): DietRecordCardUiModel {
    return DietRecordCardUiModel(
        id = id,
        savedAt = savedAt,
        summary = summary,
        totalCalories = totalCalories,
        dominantTag = dominantTag.toFoodTagLevel()
    )
}

/**
 * 后端标签颜色转换为前端枚举。
 */
private fun Int.toFoodTagLevel(): com.dietrecord.app.core.model.FoodTagLevel {
    return when (this) {
        1 -> com.dietrecord.app.core.model.FoodTagLevel.Light
        3 -> com.dietrecord.app.core.model.FoodTagLevel.High
        else -> com.dietrecord.app.core.model.FoodTagLevel.Balanced
    }
}

/**
 * 当识别会话缺少标准保存项时，使用页面识别结果兜底组装请求。
 */
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

/**
 * UI 标签级别转换为后端颜色编码。
 */
private fun FoodTagLevel.toTagColor(): Int {
    return when (this) {
        FoodTagLevel.Light -> 1
        FoodTagLevel.High -> 3
        FoodTagLevel.Balanced -> 2
    }
}
