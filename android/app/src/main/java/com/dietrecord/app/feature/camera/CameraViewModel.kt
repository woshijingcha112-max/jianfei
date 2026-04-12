package com.dietrecord.app.feature.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.data.RecognitionRepository
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "CameraViewModel"

data class CameraUiState(
    val isRecognizing: Boolean = false,
    val errorMessage: String? = null,
    val simulateNextFailure: Boolean = false
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

    fun recognizeCapturedPhoto(photoFile: File) {
        viewModelScope.launch {
            val startNanos = System.nanoTime()
            Log.i(TAG, "Start recognition for photo=${photoFile.name}")

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
                errorMessage = null
            )

            runCatching {
                recognitionRepository.recognizeCapturedPhoto(photoFile)
            }.onSuccess {
                val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
                Log.i(TAG, "Recognition finished, elapsedMs=$elapsedMs")
                _uiState.value = _uiState.value.copy(isRecognizing = false)
            }.onFailure {
                val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
                Log.e(TAG, "Recognition failed, elapsedMs=$elapsedMs", it)
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    errorMessage = "上传或识别失败，请稍后再试。"
                )
            }
        }
    }

    fun reportError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isRecognizing = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
