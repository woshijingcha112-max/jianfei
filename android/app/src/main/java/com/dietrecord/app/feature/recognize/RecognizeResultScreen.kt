package com.dietrecord.app.feature.recognize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppRoundIconBadge
import com.dietrecord.app.core.ui.components.AppSecondaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.components.MetricRow
import com.dietrecord.app.core.ui.components.TagBadge
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CandyOrange
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.StrawberryRed

@Composable
fun RecognizeResultScreen(
    uiState: RecognizeResultUiState,
    onSave: () -> Unit,
    onConsumeNavigation: () -> Unit,
    onToggleSimulateFailure: () -> Unit
) {
    LaunchedEffect(uiState.navigateHome) {
        if (uiState.navigateHome) {
            onConsumeNavigation()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppPageHeader(
                title = "识别结果",
                subtitle = "列表结构保持不动，只把结果卡、Tag 和主按钮视觉统一到首页语言里。",
                badgeText = if (uiState.items.isEmpty()) "空结果" else "${uiState.items.size} 项"
            )
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
            AppSectionCard(
                eyebrow = "识别汇总",
                title = "保存前先确认热量估算"
            ) {
                MetricRow(label = "识别项数", value = "${uiState.items.size}")
                MetricRow(label = "预计热量", value = "${uiState.totalCalories} kcal")
                Text(
                    text = "本轮先保留固定 mock 结果，不做人工修正和删改交互。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CocoaBrown.copy(alpha = 0.72f)
                )
            }
        }

        if (uiState.items.isEmpty()) {
            item {
                AppSectionCard(
                    eyebrow = "暂无结果",
                    title = "先回到拍照页生成示例识别"
                ) {
                    Text(
                        text = "识别结果为空时，不扩展兜底流程，仍然回到现有 MVP 主链路处理。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CocoaBrown.copy(alpha = 0.72f)
                    )
                }
            }
        } else {
            itemsIndexed(uiState.items, key = { _, item -> item.id }) { index, item ->
                RecognizeItemCard(index = index, item = item)
            }
        }

        item {
            AppPrimaryButton(
                text = if (uiState.isSaving) "保存中..." else "确认保存到今日饮食",
                onClick = onSave,
                enabled = uiState.items.isNotEmpty() && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                supportingText = "保存后直接回首页，看总览卡和记录卡刷新。",
                badgeText = "存"
            )
        }

        item {
            AppSecondaryButton(
                text = if (uiState.simulateNextFailure) "已开启：下次保存失败" else "模拟下次保存失败",
                onClick = onToggleSimulateFailure,
                modifier = Modifier.fillMaxWidth(),
                supportingText = "只保留最小调试入口，不新增额外结果操作。",
                badgeText = "测"
            )
        }
    }
}

@Composable
private fun RecognizeItemCard(
    index: Int,
    item: RecognizedFoodUiModel
) {
    val accent = when (index % 3) {
        0 -> AccentVisual("蛋", Color(0xFFFFF3D7), CandyOrange)
        1 -> AccentVisual("蔬", Color(0xFFE8F8E7), Color(0xFF4EB776))
        else -> AccentVisual("饭", PetalPink, StrawberryRed)
    }
    AppSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppRoundIconBadge(
                text = accent.label,
                containerColor = accent.container,
                contentColor = accent.content
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = CocoaBrown
                )
                AppAccentBadge(
                    text = "${item.calories} kcal",
                    containerColor = accent.container.copy(alpha = 0.82f),
                    contentColor = accent.content
                )
            }
            TagBadge(level = item.tagLevel)
        }
    }
}

private data class AccentVisual(
    val label: String,
    val container: Color,
    val content: Color
)
