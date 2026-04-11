package com.dietrecord.app.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppSecondaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite

@Composable
fun CameraScreen(
    uiState: CameraUiState,
    onRecognize: () -> Unit,
    onConsumeNavigation: () -> Unit,
    onToggleSimulateFailure: () -> Unit,
    onDismissError: () -> Unit
) {
    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onConsumeNavigation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppPageHeader(
            title = "拍照识别",
            subtitle = "保持当前结构，只把标题区、示意图容器和按钮换成同一套奶油粉语言。",
            badgeText = "MOCK"
        )

        SampleMealPreview(modifier = Modifier.fillMaxWidth())

        AppSectionCard(
            eyebrow = "当前链路",
            title = "示例餐图会直接进入 mock 识别"
        ) {
            Text(
                text = "这轮不接 CameraX，也不改现有流程。点击主按钮后，会直接生成固定识别结果并进入结果页。",
                style = MaterialTheme.typography.bodyLarge,
                color = CocoaBrown.copy(alpha = 0.74f)
            )
            if (uiState.errorMessage != null) {
                InlineFeedback(message = uiState.errorMessage, isError = true)
                AppSecondaryButton(
                    text = "清除提示",
                    onClick = onDismissError,
                    modifier = Modifier.fillMaxWidth(),
                    badgeText = "清"
                )
            }
        }

        AppPrimaryButton(
            text = if (uiState.isRecognizing) "识别中..." else "使用示例餐图开始识别",
            onClick = onRecognize,
            enabled = !uiState.isRecognizing,
            modifier = Modifier.fillMaxWidth(),
            supportingText = "继续保留 MVP 最小链路，不额外扩展交互。",
            badgeText = "识"
        )

        AppSecondaryButton(
            text = if (uiState.simulateNextFailure) "已开启：下次识别失败" else "模拟下次识别失败",
            onClick = onToggleSimulateFailure,
            modifier = Modifier.fillMaxWidth(),
            supportingText = "只用于验证失败提示，不影响正式主链路结构。",
            badgeText = "测"
        )
    }
}

@Composable
private fun SampleMealPreview(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(280.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF1EE),
                        Color(0xFFFFE0D6)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFBF7),
                            Color(0xFFFFE9D1)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x33F28FB0))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.72f)
                    .height(132.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF7FAFF))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.6f)
                    .height(108.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFE3A8))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.34f)
                    .height(74.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFFAF0))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.12f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF3B63C))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-88).dp, y = 2.dp)
                    .fillMaxWidth(0.18f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFDC6B59))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 86.dp, y = (-12).dp)
                    .fillMaxWidth(0.22f)
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF83A44B))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 72.dp, y = 12.dp)
                    .fillMaxWidth(0.16f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE55F50))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 106.dp, y = 20.dp)
                    .fillMaxWidth(0.13f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE55F50))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-108).dp, y = 22.dp)
                    .fillMaxWidth(0.1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF84AA52))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CreamWhite.copy(alpha = 0.9f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CreamWhite.copy(alpha = 0.9f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CreamWhite.copy(alpha = 0.9f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CreamWhite.copy(alpha = 0.9f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(CreamWhite.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "示例餐图",
                style = MaterialTheme.typography.labelLarge,
                color = BlossomPink
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppAccentBadge(
                text = "鸡蛋 + 番茄 + 炒饭",
                containerColor = CreamWhite.copy(alpha = 0.94f),
                contentColor = CocoaBrown
            )
            Text(
                text = "当前只用于触发 mock 识别流程",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CocoaBrown,
                textAlign = TextAlign.Center
            )
        }
    }
}
