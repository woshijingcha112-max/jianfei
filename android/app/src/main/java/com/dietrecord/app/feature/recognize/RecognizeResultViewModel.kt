package com.dietrecord.app.feature.recognize

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.data.AppRefreshCoordinator
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.data.DietRecordRepository
import com.dietrecord.app.data.RecognitionRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "RecognizeResultVM"

/**
 * 识别结果页状态模型。
 */
data class RecognizeResultUiState(
    val items: List<RecognizedFoodUiModel> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val simulateNextFailure: Boolean = false,
    val navigateHome: Boolean = false
) {
    val totalCalories: Int = items.sumOf { it.calories }
}

/**
 * 识别结果页 ViewModel。
 *
 * 负责接收识别会话结果，并把确认保存动作落到今日饮食记录。
 */
class RecognizeResultViewModel(
    private val recognitionRepository: RecognitionRepository,
    private val dietRecordRepository: DietRecordRepository,
    private val refreshCoordinator: AppRefreshCoordinator
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecognizeResultUiState())
    val uiState: StateFlow<RecognizeResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recognitionRepository.currentRecognitionFlow.collect { items ->
                Log.d(TAG, "识别结果流更新，条目数=${items.size}")
                _uiState.value = _uiState.value.copy(items = items)
            }
        }
    }

    fun prepareForNewRecognition() {
        _uiState.value = _uiState.value.copy(
            items = emptyList(),
            isSaving = false,
            errorMessage = null,
            successMessage = null,
            navigateHome = false
        )
        viewModelScope.launch {
            recognitionRepository.clearCurrentRecognition()
        }
    }

    fun toggleSimulateNextFailure() {
        Log.d(TAG, "切换识别结果保存失败模拟开关，当前=${!_uiState.value.simulateNextFailure}")
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveRecord() {
        val items = _uiState.value.items
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "当前没有可保存的识别结果。"
            )
            return
        }

        viewModelScope.launch {
            if (_uiState.value.simulateNextFailure) {
                Log.w(TAG, "命中识别结果保存失败模拟")
                _uiState.value = _uiState.value.copy(
                    simulateNextFailure = false,
                    errorMessage = "模拟保存失败，请再次点击保存。",
                    successMessage = null
                )
                return@launch
            }

            Log.i(TAG, "开始保存识别结果，条目数=${items.size}")
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null,
                navigateHome = false
            )

            runCatching {
                dietRecordRepository.saveRecognizedRecord(
                    items = items,
                    timestamp = LocalDateTime.now()
                )
                recognitionRepository.clearCurrentRecognition()
            }.onSuccess {
                Log.i(TAG, "识别结果保存完成，准备返回首页")
                refreshCoordinator.markMutationSuccess(source = "recognize")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "已加入今日饮食记录。",
                    navigateHome = true
                )
            }.onFailure {
                Log.e(TAG, "识别结果保存失败", it)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败，请稍后再试。"
                )
            }
        }
    }

    fun onNavigationHandled() {
        Log.d(TAG, "首页返回导航状态已消费")
        _uiState.value = _uiState.value.copy(
            navigateHome = false,
            successMessage = null
        )
    }
}
