package com.dietrecord.app.feature.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.AppTextField
import com.dietrecord.app.core.ui.components.InlineFeedback

@Composable
fun GoalScreen(
    uiState: GoalUiState,
    onCurrentWeightChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onDailyLimitChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppPageHeader(
                title = "目标",
                onBackClick = onNavigateBack
            )
        }

        item {
            AppSectionCard {
                AppTextField(
                    value = uiState.currentWeightInput,
                    onValueChange = onCurrentWeightChange,
                    label = "当前体重 kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                AppTextField(
                    value = uiState.targetWeightInput,
                    onValueChange = onTargetWeightChange,
                    label = "目标体重 kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                AppTextField(
                    value = uiState.dailyLimitInput,
                    onValueChange = onDailyLimitChange,
                    label = "每日热量上限 kcal",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        if (uiState.errorMessage != null) {
            item {
                InlineFeedback(message = uiState.errorMessage, isError = true)
            }
        }
        if (uiState.successMessage != null) {
            item {
                InlineFeedback(message = uiState.successMessage, isError = false)
            }
        }

        item {
            AppPrimaryButton(
                text = if (uiState.isSaving) "保存中..." else "保存",
                onClick = onSave,
                enabled = !uiState.isLoading && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
