package com.dietrecord.app.feature.home

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

class HomeViewModel(
    private val dietRecordRepository: DietRecordRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTodayRecords()
        refreshToday()
    }

    private fun observeTodayRecords() {
        viewModelScope.launch {
            dietRecordRepository.todayRecordsFlow.collectLatest { records ->
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

    private fun refreshToday() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                dietRecordRepository.listTodayRecords(LocalDate.now())
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "首页数据加载失败，请稍后再试。"
                )
            }
        }
    }
}
