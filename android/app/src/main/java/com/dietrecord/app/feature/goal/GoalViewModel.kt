package com.dietrecord.app.feature.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.model.GoalUiModel
import com.dietrecord.app.data.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class GoalViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                goalRepository.getGoal()
            }.onSuccess { goal ->
                _uiState.value = _uiState.value.copy(
                    currentWeightInput = goal.currentWeightKg.stripZero(),
                    targetWeightInput = goal.targetWeightKg.stripZero(),
                    dailyLimitInput = goal.dailyCalorieLimit.toString(),
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "目标数据加载失败，请稍后再试。"
                )
            }
        }
    }

    fun updateCurrentWeight(value: String) {
        _uiState.value = _uiState.value.copy(currentWeightInput = value, errorMessage = null, successMessage = null)
    }

    fun updateTargetWeight(value: String) {
        _uiState.value = _uiState.value.copy(targetWeightInput = value, errorMessage = null, successMessage = null)
    }

    fun updateDailyLimit(value: String) {
        _uiState.value = _uiState.value.copy(dailyLimitInput = value, errorMessage = null, successMessage = null)
    }

    fun toggleSimulateNextFailure() {
        _uiState.value = _uiState.value.copy(
            simulateNextFailure = !_uiState.value.simulateNextFailure,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveGoal() {
        val targetWeight = _uiState.value.targetWeightInput.toDoubleOrNull()
        val dailyLimit = _uiState.value.dailyLimitInput.toIntOrNull()

        if (targetWeight == null || dailyLimit == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "请输入有效的目标体重和热量数字。")
            return
        }
        if (dailyLimit <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "每日热量上限必须大于 0。")
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

            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
            runCatching {
                goalRepository.saveGoal(
                    GoalUiModel(
                        currentWeightKg = _uiState.value.currentWeightInput.toDoubleOrNull() ?: 0.0,
                        targetWeightKg = targetWeight,
                        dailyCalorieLimit = dailyLimit
                    )
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "目标已更新，首页会同步显示最新值。"
                )
            }.onFailure {
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
