package com.dietrecord.app.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.data.DietRecordRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

/**
 * 首页状态模型。
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val summary: HomeSummaryUiModel = HomeSummaryUiModel(
        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
        consumedCalories = 0,
        targetCalories = 1500,
        records = emptyList()
    ),
    val records: List<DietRecordCardUiModel> = emptyList()
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && records.isEmpty()
}

/**
 * 首页 ViewModel。
 *
 * 负责把 Repository 的今日记录和统计结果组装成首页 UI 状态。
 */
class HomeViewModel(
    private val dietRecordRepository: DietRecordRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTodayRecords()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            Log.i(TAG, "开始刷新首页今日数据")
            runCatching {
                dietRecordRepository.listTodayRecords(LocalDate.now())
            }.onSuccess {
                Log.i(TAG, "首页今日数据刷新完成")
            }.onFailure {
                Log.e(TAG, "首页今日数据刷新失败", it)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "首页数据加载失败，请稍后再试。"
                )
            }
        }
    }

    private fun observeTodayRecords() {
        viewModelScope.launch {
            Log.d(TAG, "开始监听今日饮食记录流")
            dietRecordRepository.todayRecordsFlow.collectLatest { records ->
                Log.d(TAG, "收到今日饮食记录更新，记录数=${records.size}")
                val summary = runCatching {
                    dietRecordRepository.getTodaySummary(LocalDate.now())
                }.getOrElse {
                    _uiState.value.summary.copy(records = records)
                }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = null,
                    summary = summary.copy(records = records),
                    records = records
                )
            }
        }
    }
}
