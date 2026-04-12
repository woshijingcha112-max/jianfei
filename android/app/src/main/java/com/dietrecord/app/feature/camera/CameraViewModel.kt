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

/**
 * 拍照页状态模型。
 */
data class CameraUiState(
    val isRecognizing: Boolean = false,
    val errorMessage: String? = null,
    val simulateNextFailure: Boolean = false,
    val navigateToResult: Boolean = false
)

/**
 * 拍照页 ViewModel。
 *
 * 负责驱动拍照上传、识别跳转和错误提示。
 */
class CameraViewModel(
    private val recognitionRepository: RecognitionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun toggleSimulateNextFailure() {
        Log.d(TAG, "切换拍照识别失败模拟开关，当前=${!_uiState.value.simulateNextFailure}")
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null
        )
    }

    fun recognizeCapturedPhoto(photoFile: File) {
        viewModelScope.launch {
            val startNanos = System.nanoTime()
            Log.i(TAG, "开始处理拍照识别，file=${photoFile.name}")

            if (_uiState.value.simulateNextFailure) {
                Log.w(TAG, "命中拍照识别失败模拟")
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
                recognitionRepository.recognizeCapturedPhoto(photoFile)
            }.onSuccess {
                val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
                Log.i(TAG, "拍照识别成功，准备跳转到结果页，elapsedMs=$elapsedMs")
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    navigateToResult = true
                )
            }.onFailure {
                val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
                Log.e(TAG, "拍照识别失败，elapsedMs=$elapsedMs", it)
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    errorMessage = "上传或识别失败，请稍后再试。"
                )
            }
        }
    }

    fun reportError(message: String) {
        Log.w(TAG, "拍照页收到错误提示：$message")
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isRecognizing = false
        )
    }

    fun onNavigationHandled() {
        Log.d(TAG, "识别结果页导航状态已消费")
        _uiState.value = _uiState.value.copy(navigateToResult = false)
    }

    fun clearError() {
        Log.d(TAG, "清理拍照页错误提示")
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
