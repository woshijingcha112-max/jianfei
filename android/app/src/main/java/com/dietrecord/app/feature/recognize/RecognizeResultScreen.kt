package com.dietrecord.app.feature.recognize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dietrecord.app.core.model.RecognizedFoodUiModel
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppRoundIconBadge
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.components.MetricRow
import com.dietrecord.app.core.ui.components.TagBadge
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CandyOrange
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.ErrorRose
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.StrawberryRed
import com.dietrecord.app.core.ui.theme.SuccessMint

@Composable
fun RecognizeResultScreen(
    uiState: RecognizeResultUiState,
    isRecognitionPending: Boolean,
    recognitionErrorMessage: String?,
    onSave: () -> Unit,
    onRetake: () -> Unit,
    onConsumeNavigation: () -> Unit
) {
    LaunchedEffect(uiState.navigateHome) {
        if (uiState.navigateHome) {
            onConsumeNavigation()
        }
    }

    val showRecognitionLoading =
        isRecognitionPending && uiState.items.isEmpty() && recognitionErrorMessage == null
    val saveEnabled = uiState.items.isNotEmpty() && !uiState.isSaving && !showRecognitionLoading
    val retakeEnabled = !isRecognitionPending && !uiState.isSaving

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppPageHeader(
                    title = "识别结果",
                    subtitle = "本次拍照已提交识别，后端返回后会在这里更新内容。",
                    badgeText = when {
                        showRecognitionLoading -> "识别中"
                        uiState.items.isEmpty() -> "空结果"
                        else -> "${uiState.items.size} 项"
                    }
                )
            }

            if (recognitionErrorMessage != null) {
                item {
                    InlineFeedback(message = recognitionErrorMessage, isError = true)
                }
            }
            if (uiState.errorMessage != null) {
                item {
                    InlineFeedback(message = uiState.errorMessage, isError = true)
                }
            }
            if (showRecognitionLoading) {
                item {
                    InlineFeedback(message = "图片已提交，正在等待后端识别结果。", isError = false)
                }
            }

            item {
                AppSectionCard(
                    eyebrow = "识别汇总",
                    title = "保存前确认当前结果"
                ) {
                    MetricRow(
                        label = "识别项目数",
                        value = if (showRecognitionLoading) "--" else "${uiState.items.size}"
                    )
                    MetricRow(
                        label = "预计热量",
                        value = if (showRecognitionLoading) "--" else "${uiState.totalCalories} kcal"
                    )
                    Text(
                        text = when {
                            showRecognitionLoading -> "正在异步等待后端返回，明细区先显示占位。"
                            recognitionErrorMessage != null -> "本次识别失败，可以直接重新拍照。"
                            else -> "确认无误后可直接保存到今日饮食记录。"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = CocoaBrown.copy(alpha = 0.72f)
                    )
                }
            }

            if (showRecognitionLoading) {
                item {
                    RecognitionLoadingSection()
                }
            } else if (uiState.items.isEmpty()) {
                item {
                    AppSectionCard(
                        eyebrow = "暂无结果",
                        title = "当前还没有可保存的识别明细"
                    ) {
                        Text(
                            text = "可以重新拍照，或等待后端继续返回。",
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
        }

        RecognizeFloatingActions(
            saveEnabled = saveEnabled,
            isSaving = uiState.isSaving,
            retakeEnabled = retakeEnabled,
            onSave = onSave,
            onRetake = onRetake,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun RecognizeFloatingActions(
    saveEnabled: Boolean,
    isSaving: Boolean,
    retakeEnabled: Boolean,
    onSave: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CreamWhite.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessMint,
                    contentColor = Color(0xFF2E6E46),
                    disabledContainerColor = SuccessMint.copy(alpha = 0.55f),
                    disabledContentColor = Color(0xFF2E6E46).copy(alpha = 0.55f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF2E6E46),
                        strokeWidth = 2.dp,
                        trackColor = Color(0xFF2E6E46).copy(alpha = 0.18f)
                    )
                } else {
                    Text(
                        text = "确认保存",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Button(
                onClick = onRetake,
                enabled = retakeEnabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRose,
                    contentColor = StrawberryRed,
                    disabledContainerColor = ErrorRose.copy(alpha = 0.55f),
                    disabledContentColor = StrawberryRed.copy(alpha = 0.55f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "重新拍照",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun RecognitionLoadingSection() {
    AppSectionCard(
        eyebrow = "识别中",
        title = "明细结果正在返回"
    ) {
        repeat(3) { index ->
            RecognitionLoadingRow(isLast = index == 2)
        }
    }
}

@Composable
private fun RecognitionLoadingRow(isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(BlossomPink.copy(alpha = 0.14f), CircleShape)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(14.dp)
                    .background(CocoaBrown.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(10.dp)
                    .background(CocoaBrown.copy(alpha = 0.08f), RoundedCornerShape(5.dp))
            )
        }
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = BlossomPink,
            strokeWidth = 2.dp,
            trackColor = BlossomPink.copy(alpha = 0.18f)
        )
    }
    if (!isLast) {
        Spacer(modifier = Modifier.height(14.dp))
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
        else -> AccentVisual("饮", PetalPink, StrawberryRed)
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
