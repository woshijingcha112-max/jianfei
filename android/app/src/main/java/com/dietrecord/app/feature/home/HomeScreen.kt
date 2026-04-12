package com.dietrecord.app.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppRoundIconBadge
import com.dietrecord.app.core.ui.components.AppSecondaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.components.TagBadge
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CandyOrange
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.StrawberryRed
import com.dietrecord.app.core.ui.theme.WarningPeach

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenGoal: () -> Unit,
    onOpenCamera: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val horizontalPadding = if (configuration.screenWidthDp <= 412) 16.dp else 20.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppPageHeader(title = "今日饮食")
        }

        item {
            HomeOverviewCard(
                summary = uiState.summary,
                onOpenGoal = onOpenGoal
            )
        }

        item {
            AppPrimaryButton(
                text = "拍照记录",
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.errorMessage != null) {
            item {
                InlineFeedback(message = uiState.errorMessage, isError = true)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当天记录",
                    style = MaterialTheme.typography.titleLarge,
                    color = CocoaBrown
                )
                AppAccentBadge(
                    text = if (uiState.records.isEmpty()) "暂无记录" else "${uiState.records.size} 条"
                )
            }
        }

        if (uiState.isEmpty) {
            item {
                AppSectionCard(title = "今天还没有记录") {
                    Text(
                        text = "拍一餐后会显示在这里。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CocoaBrown.copy(alpha = 0.74f)
                    )
                }
            }
        } else {
            itemsIndexed(uiState.records, key = { _, record -> record.id }) { index, record ->
                HomeRecordCard(
                    index = index,
                    record = record
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewCard(
    summary: HomeSummaryUiModel,
    onOpenGoal: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isCompactWidth = configuration.screenWidthDp <= 360
    val isPhoneCompactWidth = configuration.screenWidthDp <= 412
    val remainingCalories = (summary.targetCalories - summary.consumedCalories).coerceAtLeast(0)
    val cardPadding = if (isCompactWidth) 14.dp else 18.dp
    val outerCardPadding = if (isPhoneCompactWidth) 14.dp else 18.dp
    val sectionSpacing = if (isCompactWidth) 14.dp else 16.dp
    val metricSpacing = if (isCompactWidth) 8.dp else 12.dp
    val summaryTitleStyle = if (isCompactWidth) {
        MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp)
    } else {
        MaterialTheme.typography.headlineMedium
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 16.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE6EF),
                            Color(0xFFFFD9E6)
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(outerCardPadding)
        ) {
            BoxWithConstraints {
                val estimatedMetricWidth = (maxWidth - metricSpacing) / 2
                val shouldStackMetrics = estimatedMetricWidth < 176.dp || configuration.fontScale >= 1.15f

                Column(verticalArrangement = Arrangement.spacedBy(sectionSpacing)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppAccentBadge(
                            text = summary.dateLabel,
                            containerColor = CreamWhite.copy(alpha = 0.9f),
                            contentColor = BlossomPink
                        )
                        Text(
                            text = "今日总览",
                            style = summaryTitleStyle,
                            color = CocoaBrown
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CreamWhite.copy(alpha = 0.96f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = cardPadding, vertical = cardPadding),
                            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                        ) {
                            if (shouldStackMetrics) {
                                Column(verticalArrangement = Arrangement.spacedBy(metricSpacing)) {
                                    OverviewMetricBlock(
                                        modifier = Modifier.fillMaxWidth(),
                                        label = "已摄入",
                                        value = "${summary.consumedCalories}",
                                        unit = "kcal",
                                        accent = CandyOrange,
                                        compactLayout = true
                                    )
                                    OverviewMetricBlock(
                                        modifier = Modifier.fillMaxWidth(),
                                        label = "目标",
                                        value = "${summary.targetCalories}",
                                        unit = "kcal",
                                        accent = BlossomPink,
                                        compactLayout = true
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(metricSpacing)
                                ) {
                                    OverviewMetricBlock(
                                        modifier = Modifier.weight(1f),
                                        label = "已摄入",
                                        value = "${summary.consumedCalories}",
                                        unit = "kcal",
                                        accent = CandyOrange,
                                        compactLayout = isPhoneCompactWidth
                                    )
                                    OverviewMetricBlock(
                                        modifier = Modifier.weight(1f),
                                        label = "目标",
                                        value = "${summary.targetCalories}",
                                        unit = "kcal",
                                        accent = BlossomPink,
                                        compactLayout = isPhoneCompactWidth
                                    )
                                }
                            }

                            Text(
                                text = if (remainingCalories > 0) {
                                    "今天还可以摄入 $remainingCalories kcal"
                                } else {
                                    "今天已达到目标"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = CocoaBrown
                            )

                            GoalAnchoredProgressBar(
                                consumedCalories = summary.consumedCalories,
                                targetCalories = summary.targetCalories,
                                compactLayout = shouldStackMetrics || isPhoneCompactWidth,
                                modifier = Modifier.fillMaxWidth()
                            )

                            AppSecondaryButton(
                                text = "调整目标",
                                onClick = onOpenGoal,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewMetricBlock(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    compactLayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    val valueStyle = if (compactLayout) {
        MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp)
    } else {
        MaterialTheme.typography.headlineMedium
    }
    val unitStyle = if (compactLayout) {
        MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp)
    } else {
        MaterialTheme.typography.labelLarge
    }

    Surface(
        modifier = modifier,
        color = WarningPeach.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = CocoaBrown.copy(alpha = 0.72f)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = accent,
                            fontSize = valueStyle.fontSize,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = valueStyle.letterSpacing
                        )
                    ) {
                        append(value)
                    }
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = CocoaBrown.copy(alpha = 0.7f),
                            fontSize = unitStyle.fontSize,
                            fontWeight = unitStyle.fontWeight,
                            letterSpacing = unitStyle.letterSpacing
                        )
                    ) {
                        append(unit)
                    }
                },
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun HomeRecordCard(
    index: Int,
    record: DietRecordCardUiModel
) {
    val mealVisual = mealVisualOf(record.savedAt, index)
    AppSectionCard(
        eyebrow = mealVisual.label,
        title = "${record.totalCalories} kcal"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            AppRoundIconBadge(
                text = mealVisual.shortLabel,
                containerColor = mealVisual.container,
                contentColor = mealVisual.content
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppAccentBadge(
                    text = "保存时间 ${record.savedAt}",
                    containerColor = mealVisual.container.copy(alpha = 0.72f),
                    contentColor = mealVisual.content
                )
                Text(
                    text = record.summary,
                    style = MaterialTheme.typography.titleMedium,
                    color = CocoaBrown
                )
                TagBadge(level = record.dominantTag)
            }
        }
    }
}

@Composable
private fun GoalAnchoredProgressBar(
    consumedCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    compactLayout: Boolean = false,
    anchorRatio: Float = 0.82f,
    maxOverflowMultiplier: Float = 2f,
    barHeight: Dp = 16.dp
) {
    val safeTarget = targetCalories.coerceAtLeast(1)
    val safeConsumed = consumedCalories.coerceAtLeast(0)
    val remaining = (safeTarget - safeConsumed).coerceAtLeast(0)
    val normalizedProgress = when {
        safeConsumed <= safeTarget -> {
            (safeConsumed.toFloat() / safeTarget.toFloat()) * anchorRatio
        }

        else -> {
            val overflowRange = safeTarget * (maxOverflowMultiplier - 1f)
            val overflowProgress = if (overflowRange <= 0f) {
                1f
            } else {
                ((safeConsumed - safeTarget).toFloat() / overflowRange).coerceIn(0f, 1f)
            }
            anchorRatio + ((1f - anchorRatio) * overflowProgress)
        }
    }.coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "当前 ${safeConsumed} kcal",
                style = MaterialTheme.typography.labelLarge,
                color = CocoaBrown
            )
            Text(
                modifier = Modifier.weight(1f),
                text = if (remaining > 0) "剩余 $remaining kcal" else "已达目标",
                style = MaterialTheme.typography.labelLarge,
                color = if (remaining > 0) BlossomPink else StrawberryRed,
                textAlign = TextAlign.End
            )
        }

        BoxWithConstraints(modifier = modifier) {
            val badgeWidth = when {
                safeTarget >= 1000 -> if (compactLayout) 116.dp else 124.dp
                compactLayout -> 92.dp
                else -> 96.dp
            }
            val maxOffset = (maxWidth - badgeWidth).coerceAtLeast(0.dp)
            val badgeOffset = ((maxWidth * anchorRatio) - (badgeWidth / 2)).coerceIn(0.dp, maxOffset)

            AppAccentBadge(
                text = "目标 ${safeTarget}",
                modifier = Modifier.offset(x = badgeOffset),
                containerColor = Color(0xFFFFF0E1),
                contentColor = CandyOrange
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .height(barHeight)
            ) {
                val radius = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(
                    color = Color(0xFFFFE5D8),
                    size = size,
                    cornerRadius = radius
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD96A),
                            CandyOrange,
                            StrawberryRed
                        )
                    ),
                    size = Size(width = size.width * normalizedProgress, height = size.height),
                    cornerRadius = radius
                )
                val markerX = size.width * anchorRatio
                drawLine(
                    color = BlossomPink,
                    start = Offset(markerX, 0f),
                    end = Offset(markerX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

private fun mealVisualOf(savedAt: String, index: Int): MealVisual {
    val hour = savedAt.substringBefore(':').toIntOrNull()
    return when {
        hour != null && hour < 10 -> MealVisual("早餐记录", "早", Color(0xFFFFF1D7), CandyOrange)
        hour != null && hour < 15 -> MealVisual("午餐记录", "午", Color(0xFFFFE5DE), StrawberryRed)
        hour != null && hour < 18 -> MealVisual("加餐记录", "加", Color(0xFFE6F3FF), Color(0xFF4A98E8))
        hour != null -> MealVisual("晚餐记录", "晚", PetalPink, BlossomPink)
        else -> {
            when (index % 3) {
                0 -> MealVisual("早餐记录", "早", Color(0xFFFFF1D7), CandyOrange)
                1 -> MealVisual("午餐记录", "午", Color(0xFFFFE5DE), StrawberryRed)
                else -> MealVisual("晚餐记录", "晚", PetalPink, BlossomPink)
            }
        }
    }
}

private data class MealVisual(
    val label: String,
    val shortLabel: String,
    val container: Color,
    val content: Color
)
