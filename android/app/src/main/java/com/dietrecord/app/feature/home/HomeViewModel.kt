package com.dietrecord.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.data.DietRecordRepository
import com.dietrecord.app.data.GoalRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: HomeSummaryUiModel = HomeSummaryUiModel(
        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
        consumedCalories = 0,
        targetCalories = 1500,
        records = emptyList()
    ),
    val records: List<DietRecordCardUiModel> = emptyList()
) {
    val isEmpty: Boolean = !isLoading && records.isEmpty()
}

class HomeViewModel(
    goalRepository: GoalRepository,
    dietRecordRepository: DietRecordRepository
) : ViewModel() {
    private val mockConsumedBaseline = 1300

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                goalRepository.goalFlow,
                dietRecordRepository.todayRecordsFlow
            ) { goal, records ->
                HomeUiState(
                    isLoading = false,
                    summary = HomeSummaryUiModel(
                        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                        consumedCalories = mockConsumedBaseline + records.sumOf { it.totalCalories },
                        targetCalories = goal.dailyCalorieLimit,
                        records = records
                    ),
                    records = records
                )
            }.collect { _uiState.value = it }
        }
    }
}
