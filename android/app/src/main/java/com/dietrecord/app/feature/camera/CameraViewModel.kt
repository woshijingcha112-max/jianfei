package com.dietrecord.app.feature.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.data.RecognitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraUiState(
    val isRecognizing: Boolean = false,
    val errorMessage: String? = null,
    val simulateNextFailure: Boolean = false,
    val navigateToResult: Boolean = false
)

class CameraViewModel(
    private val recognitionRepository: RecognitionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun toggleSimulateNextFailure() {
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null
        )
    }

    fun recognizeSamplePhoto() {
        viewModelScope.launch {
            if (_uiState.value.simulateNextFailure) {
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    errorMessage = "模拟识别失败，请重试。",
                    simulateNextFailure = false
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isRecognizing = true,
                errorMessage = null,
                navigateToResult = false
            )

            runCatching {
                recognitionRepository.recognizeSamplePhoto()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    navigateToResult = true
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    errorMessage = "上传或识别失败，请稍后再试。"
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToResult = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
