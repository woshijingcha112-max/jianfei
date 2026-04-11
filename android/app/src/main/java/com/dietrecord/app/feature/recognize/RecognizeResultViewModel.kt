package com.dietrecord.app.feature.recognize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.data.DietRecordRepository
import com.dietrecord.app.data.RecognitionRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class RecognizeResultViewModel(
    private val recognitionRepository: RecognitionRepository,
    private val dietRecordRepository: DietRecordRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecognizeResultUiState())
    val uiState: StateFlow<RecognizeResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recognitionRepository.currentRecognitionFlow.collect { items ->
                _uiState.value = _uiState.value.copy(items = items)
            }
        }
    }

    fun toggleSimulateNextFailure() {
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveRecord() {
        val items = _uiState.value.items
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "当前没有可保存的识别结果。")
            return
        }

        viewModelScope.launch {
            if (_uiState.value.simulateNextFailure) {
                _uiState.value = _uiState.value.copy(
                    simulateNextFailure = false,
                    errorMessage = "模拟保存失败，请再次点击保存。",
                    successMessage = null
                )
                return@launch
            }

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
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "已加入今日饮食记录。",
                    navigateHome = true
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败，请稍后再试。"
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateHome = false,
            successMessage = null
        )
    }
}
