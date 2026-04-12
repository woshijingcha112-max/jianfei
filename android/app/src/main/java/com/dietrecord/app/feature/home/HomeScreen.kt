package com.dietrecord.app.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dietrecord.app.core.model.DietRecordCardUiModel
import com.dietrecord.app.core.model.HomeSummaryUiModel
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppRoundIconBadge
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.components.TagBadge
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CandyOrange
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.RibbonPink
import com.dietrecord.app.core.ui.theme.StrawberryRed
import com.dietrecord.app.core.ui.theme.SuccessMint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HomeTimeLayer()
        }

        item {
            HomeProgressCard(
                summary = uiState.summary,
                onOpenGoal = onOpenGoal
            )
        }

        if (uiState.errorMessage != null) {
            item {
                InlineFeedback(message = uiState.errorMessage, isError = true)
            }
        }

        item {
            HomeRecordHeader(
                recordCount = uiState.records.size,
                onOpenCamera = onOpenCamera
            )
        }

        if (uiState.isEmpty) {
            item {
                EmptyRecordCard()
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
private fun HomeTimeLayer() {
    val today = LocalDate.now()
    val weekStart = today.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weekDays.forEach { day ->
            WeekDayChip(
                date = day,
                isToday = day == today,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeekDayChip(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val container = if (isToday) BlossomPink else CreamWhite.copy(alpha = 0.82f)
    val content = if (isToday) CreamWhite else CocoaBrown.copy(alpha = 0.66f)

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (isToday) 6.dp else 0.dp,
        border = if (isToday) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, RibbonPink.copy(alpha = 0.35f))
        }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = date.dayOfWeek.toChineseLabel(),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeProgressCard(
    summary: HomeSummaryUiModel,
    onOpenGoal: () -> Unit
) {
    val remainingCalories = (summary.targetCalories - summary.consumedCalories).coerceAtLeast(0)
    val configuration = LocalConfiguration.current
    val isPhoneWidth = configuration.screenWidthDp <= 412
    val contentPadding = if (isPhoneWidth) 14.dp else 18.dp
    val circleSize = 130.dp
    val sideMetricWidth = if (isPhoneWidth) 58.dp else 66.dp
    val safeTarget = summary.targetCalories.coerceAtLeast(1)
    val safeConsumed = summary.consumedCalories.coerceAtLeast(0)
    val progress = (remainingCalories.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CreamWhite.copy(alpha = 0.98f),
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, RibbonPink.copy(alpha = 0.36f))
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("MM月dd日")),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = CocoaBrown
                    )
                }
                HomeTinyGoalBadge(
                    text = "调整目标",
                    onClick = onOpenGoal
                )
            }

            CalorieCircle(
                consumedCalories = safeConsumed,
                targetCalories = summary.targetCalories,
                remainingCalories = remainingCalories,
                progress = progress,
                circleSize = circleSize,
                sideMetricWidth = sideMetricWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(circleSize)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroReferenceCard(
                    label = "碳水",
                    value = "40 / 198g",
                    accent = Color(0xFF4A98E8),
                    progress = 0.2f,
                    modifier = Modifier.weight(1f)
                )
                MacroReferenceCard(
                    label = "蛋白",
                    value = "20 / 72g",
                    accent = BlossomPink,
                    progress = 0.28f,
                    modifier = Modifier.weight(1f)
                )
                MacroReferenceCard(
                    label = "脂肪",
                    value = "30 / 40g",
                    accent = CandyOrange,
                    progress = 0.75f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeTinyGoalBadge(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = CreamWhite.copy(alpha = 0.92f),
        contentColor = BlossomPink,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CalorieCircle(
    consumedCalories: Int,
    targetCalories: Int,
    remainingCalories: Int,
    progress: Float,
    circleSize: Dp,
    sideMetricWidth: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalorieSideMetric(
            label = "饮食摄入",
            value = consumedCalories,
            modifier = Modifier.width(sideMetricWidth)
        )
        Box(
            modifier = Modifier.size(circleSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                drawArc(
                    color = Color(0xFFFFEDF4),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = BlossomPink,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "还可以吃(千卡)",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
                    color = CocoaBrown.copy(alpha = 0.45f)
                )
                Text(
                    text = "$remainingCalories",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 25.sp, lineHeight = 28.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = CocoaBrown
                )
                Text(
                    text = "推荐摄入 $targetCalories",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = CocoaBrown.copy(alpha = 0.58f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
        CalorieSideMetric(
            label = "运动消耗",
            value = 0,
            modifier = Modifier.width(sideMetricWidth)
        )
    }
}

@Composable
private fun CalorieSideMetric(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = CocoaBrown.copy(alpha = 0.42f)
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 24.sp),
            fontWeight = FontWeight.ExtraBold,
            color = CocoaBrown
        )
    }
}

@Composable
private fun MacroReferenceCard(
    label: String,
    value: String,
    accent: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Surface(
            color = accent.copy(alpha = 0.12f),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 14.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = CocoaBrown
                )
                MiniProgressBar(
                    progress = progress,
                    color = accent
                )
            }
        }
    }
}

@Composable
private fun MiniProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
    ) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(
            color = color.copy(alpha = 0.18f),
            size = size,
            cornerRadius = radius
        )
        drawRoundRect(
            color = color,
            size = Size(width = size.width * progress.coerceIn(0f, 1f), height = size.height),
            cornerRadius = radius
        )
    }
}

@Composable
private fun HomeRecordHeader(
    recordCount: Int,
    onOpenCamera: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "当天记录",
                style = MaterialTheme.typography.titleLarge,
                color = CocoaBrown
            )
            Text(
                text = if (recordCount == 0) "还没有记录" else "已记录 $recordCount 条",
                style = MaterialTheme.typography.bodySmall,
                color = CocoaBrown.copy(alpha = 0.58f)
            )
        }

        HomeActionChip(
            text = "拍照记录",
            onClick = onOpenCamera
        )
    }
}

@Composable
private fun HomeActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = BlossomPink,
        contentColor = CreamWhite,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 5.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyRecordCard() {
    AppSectionCard(title = "今天还没有记录") {
        Text(
            text = "拍一餐后会显示在这里，热量会同步进入上方饮食进度。",
            style = MaterialTheme.typography.bodyLarge,
            color = CocoaBrown.copy(alpha = 0.74f)
        )
    }
}

@Composable
private fun HomeRecordCard(
    index: Int,
    record: DietRecordCardUiModel
) {
    val mealVisual = mealVisualOf(record.savedAt, index)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CreamWhite.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RibbonPink.copy(alpha = 0.36f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppRoundIconBadge(
                    text = mealVisual.shortLabel,
                    size = 44.dp,
                    cornerRadius = 16.dp,
                    containerColor = mealVisual.container,
                    contentColor = mealVisual.content
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = mealVisual.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = mealVisual.content
                    )
                    Text(
                        text = record.summary,
                        style = MaterialTheme.typography.titleMedium,
                        color = CocoaBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${record.totalCalories}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = CocoaBrown
                    )
                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = CocoaBrown.copy(alpha = 0.58f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppAccentBadge(
                    text = record.savedAt,
                    containerColor = mealVisual.container.copy(alpha = 0.72f),
                    contentColor = mealVisual.content
                )
                TagBadge(level = record.dominantTag)
            }
        }
    }
}

private fun DayOfWeek.toChineseLabel(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "一"
        DayOfWeek.TUESDAY -> "二"
        DayOfWeek.WEDNESDAY -> "三"
        DayOfWeek.THURSDAY -> "四"
        DayOfWeek.FRIDAY -> "五"
        DayOfWeek.SATURDAY -> "六"
        DayOfWeek.SUNDAY -> "日"
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
                else -> MealVisual("晚餐记录", "晚", SuccessMint, Color(0xFF4EB776))
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
