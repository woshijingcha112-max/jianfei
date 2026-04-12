package com.dietrecord.app.feature.goal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.data.AppRefreshCoordinator
import com.dietrecord.app.core.model.GoalUiModel
import com.dietrecord.app.data.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "GoalViewModel"

/**
 * 目标页状态模型。
 */
data class GoalUiState(
    val currentWeightInput: String = "",
    val targetWeightInput: String = "",
    val dailyLimitInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val simulateNextFailure: Boolean = false
)

/**
 * 目标页 ViewModel。
 *
 * 负责表单输入、保存和刷新目标数据。
 */
class GoalViewModel(
    private val goalRepository: GoalRepository,
    private val refreshCoordinator: AppRefreshCoordinator
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    fun refresh(preserveSuccessMessage: Boolean = false) {
        viewModelScope.launch {
            val retainedSuccessMessage = if (preserveSuccessMessage) {
                _uiState.value.successMessage
            } else {
                null
            }

            Log.i(TAG, "开始加载目标页数据")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = retainedSuccessMessage
            )

            runCatching {
                goalRepository.getGoal()
            }.onSuccess { goal ->
                Log.i(TAG, "目标页数据加载完成")
                _uiState.value = _uiState.value.copy(
                    currentWeightInput = goal.currentWeightKg.stripZero(),
                    targetWeightInput = goal.targetWeightKg.stripZero(),
                    dailyLimitInput = goal.dailyCalorieLimit.toString(),
                    isLoading = false,
                    successMessage = retainedSuccessMessage
                )
            }.onFailure {
                Log.e(TAG, "目标页数据加载失败", it)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "目标数据加载失败，请稍后再试。"
                )
            }
        }
    }

    fun updateCurrentWeight(value: String) {
        _uiState.value = _uiState.value.copy(
            currentWeightInput = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateTargetWeight(value: String) {
        _uiState.value = _uiState.value.copy(
            targetWeightInput = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateDailyLimit(value: String) {
        _uiState.value = _uiState.value.copy(
            dailyLimitInput = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleSimulateNextFailure() {
        Log.d(TAG, "切换目标保存失败模拟开关，当前=${!_uiState.value.simulateNextFailure}")
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveGoal() {
        val currentWeight = _uiState.value.currentWeightInput.toDoubleOrNull()
        val targetWeight = _uiState.value.targetWeightInput.toDoubleOrNull()
        val dailyLimit = _uiState.value.dailyLimitInput.toIntOrNull()

        if (currentWeight == null || targetWeight == null || dailyLimit == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请输入有效的当前体重、目标体重和热量数字。"
            )
            return
        }
        if (currentWeight <= 0 || targetWeight <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "当前体重和目标体重必须大于 0。"
            )
            return
        }
        if (dailyLimit <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "每日热量上限必须大于 0。"
            )
            return
        }

        viewModelScope.launch {
            if (_uiState.value.simulateNextFailure) {
                Log.w(TAG, "命中目标保存失败模拟")
                _uiState.value = _uiState.value.copy(
                    simulateNextFailure = false,
                    errorMessage = "模拟保存失败，请再次点击保存。",
                    successMessage = null
                )
                return@launch
            }

            Log.i(
                TAG,
                "开始保存目标设置，当前体重=$currentWeight，目标体重=$targetWeight，热量上限=$dailyLimit"
            )
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            runCatching {
                goalRepository.saveGoal(
                    GoalUiModel(
                        currentWeightKg = currentWeight,
                        targetWeightKg = targetWeight,
                        dailyCalorieLimit = dailyLimit
                    )
                )
            }.onSuccess {
                Log.i(TAG, "目标设置保存完成")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "目标已保存。"
                )
                refreshCoordinator.markMutationSuccess(source = "goal")
            }.onFailure {
                Log.e(TAG, "目标设置保存失败", it)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败，请稍后再试。"
                )
            }
        }
    }
}

private fun Double.stripZero(): String = if (this % 1.0 == 0.0) {
    toInt().toString()
} else {
    toString()
}
