package com.dietrecord.app.feature.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppSecondaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.AppTextField
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.theme.CocoaBrown

@Composable
fun GoalScreen(
    uiState: GoalUiState,
    onCurrentWeightChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onDailyLimitChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onToggleSimulateFailure: () -> Unit
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
                title = "设定目标",
                subtitle = "保留现有表单结构，只统一输入卡、保存按钮和标题区风格。",
                badgeText = "GOAL",
                onBackClick = onNavigateBack
            )
        }

        item {
            AppSectionCard(
                eyebrow = "基础目标",
                title = "保存后会同步首页总览卡"
            ) {
                Text(
                    text = "当前体重由后端真实返回，仅做展示；本轮只允许修改目标体重和每日热量上限。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CocoaBrown.copy(alpha = 0.72f)
                )
                AppTextField(
                    value = uiState.currentWeightInput,
                    onValueChange = onCurrentWeightChange,
                    label = "当前体重 kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    readOnly = true
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
                text = if (uiState.isSaving) "保存中..." else "保存目标",
                onClick = onSave,
                enabled = !uiState.isLoading && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                supportingText = "保存后首页目标热量会立即同步刷新。",
                badgeText = "存"
            )
        }

        item {
            AppSecondaryButton(
                text = if (uiState.simulateNextFailure) "已开启：下次保存失败" else "模拟下次目标保存失败",
                onClick = onToggleSimulateFailure,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                supportingText = "保留最小调试入口，不影响真实保存链路。",
                badgeText = "测"
            )
        }

        item {
            AppSectionCard(
                eyebrow = "本轮范围",
                title = "目标页只做统一风格，不做精修"
            ) {
                Text(
                    text = "如果后续继续扩 UI，会以首页已经定下来的标题头部、奶油卡片和糖果按钮语言向这里扩散。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CocoaBrown.copy(alpha = 0.72f)
                )
            }
        }
    }
}
