package com.dietrecord.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dietrecord.app.core.model.FoodTagLevel
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CandyOrange
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CottonPink
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.ErrorRose
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.RibbonPink
import com.dietrecord.app.core.ui.theme.SkyCandy
import com.dietrecord.app.core.ui.theme.StrawberryRed
import com.dietrecord.app.core.ui.theme.SuccessMint
import com.dietrecord.app.core.ui.theme.WarningPeach

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CottonPink,
                        Color(0xFFFFF8F3),
                        Color(0xFFFFF3ED)
                    )
                )
            )
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = RibbonPink.copy(alpha = 0.35f),
                radius = size.minDimension * 0.22f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.08f)
            )
            drawCircle(
                color = CandyOrange.copy(alpha = 0.18f),
                radius = size.minDimension * 0.18f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.2f)
            )
            drawCircle(
                color = BlossomPink.copy(alpha = 0.12f),
                radius = size.minDimension * 0.26f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.82f)
            )
            drawCircle(
                color = SkyCandy.copy(alpha = 0.1f),
                radius = size.minDimension * 0.14f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.9f)
            )
        }
        content()
    }
}

@Composable
fun AppPageHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            shadowElevation = 10.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFECF3),
                                Color(0xFFFFDDE8)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        if (onBackClick != null) {
                            AppRoundIconBadge(
                                text = "‹",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable(onClick = onBackClick),
                                containerColor = CreamWhite.copy(alpha = 0.92f),
                                contentColor = BlossomPink
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = CocoaBrown
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CocoaBrown.copy(alpha = 0.72f)
                            )
                        }
                        if (badgeText != null) {
                            AppAccentBadge(
                                text = badgeText,
                                containerColor = CreamWhite.copy(alpha = 0.92f),
                                contentColor = BlossomPink
                            )
                        }
                    }
                }
            }
        }
        ScallopTrim()
    }
}

@Composable
fun AppAccentBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = PetalPink,
    contentColor: Color = CocoaBrown
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun AppRoundIconBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = PetalPink,
    contentColor: Color = BlossomPink
) {
    Surface(
        modifier = modifier.size(52.dp),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun AppSectionCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = CreamWhite.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RibbonPink.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (eyebrow != null || title != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (eyebrow != null) {
                        Text(
                            text = eyebrow,
                            style = MaterialTheme.typography.labelLarge,
                            color = BlossomPink
                        )
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = CocoaBrown
                        )
                    }
                    HorizontalDivider(color = RibbonPink.copy(alpha = 0.45f))
                }
            }
            content()
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CocoaBrown.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CocoaBrown
        )
    }
}

@Composable
fun TagBadge(
    level: FoodTagLevel,
    modifier: Modifier = Modifier
) {
    val visuals = when (level) {
        FoodTagLevel.Light -> TagVisuals(
            container = SuccessMint,
            content = Color(0xFF2A6F4C),
            dot = Color(0xFF6CD28A)
        )
        FoodTagLevel.Balanced -> TagVisuals(
            container = WarningPeach,
            content = Color(0xFF9B6517),
            dot = CandyOrange
        )
        FoodTagLevel.High -> TagVisuals(
            container = ErrorRose,
            content = Color(0xFFB83A4B),
            dot = StrawberryRed
        )
    }
    Surface(
        modifier = modifier,
        color = visuals.container,
        contentColor = visuals.content,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(visuals.dot)
            )
            Text(text = level.label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun InlineFeedback(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val background = if (isError) ErrorRose else SuccessMint
    val content = if (isError) Color(0xFF9B3040) else Color(0xFF2F6D42)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = background,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            color = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    badgeText: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CandyOrange,
            contentColor = CreamWhite,
            disabledContainerColor = CandyOrange.copy(alpha = 0.45f),
            disabledContentColor = CreamWhite.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp, pressedElevation = 4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ButtonLabel(text = text, supportingText = supportingText, badgeText = badgeText)
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    badgeText: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SkyCandy,
            contentColor = CreamWhite,
            disabledContainerColor = SkyCandy.copy(alpha = 0.45f),
            disabledContentColor = CreamWhite.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 3.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ButtonLabel(text = text, supportingText = supportingText, badgeText = badgeText)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CreamWhite,
            unfocusedContainerColor = CreamWhite,
            focusedBorderColor = BlossomPink,
            unfocusedBorderColor = RibbonPink,
            focusedLabelColor = BlossomPink,
            unfocusedLabelColor = CocoaBrown.copy(alpha = 0.65f),
            cursorColor = BlossomPink
        )
    )
}

@Composable
private fun ButtonLabel(
    text: String,
    supportingText: String?,
    badgeText: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelMedium,
                    color = CreamWhite.copy(alpha = 0.88f)
                )
            }
        }
        if (badgeText != null) {
            Surface(
                color = CreamWhite.copy(alpha = 0.22f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = CreamWhite
                )
            }
        }
    }
}

@Composable
private fun ScallopTrim(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        val radius = size.height * 0.95f
        var centerX = radius
        while (centerX < size.width + radius) {
            drawCircle(
                color = Color(0xFFFFD8E5),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerX, 0f)
            )
            drawCircle(
                color = RibbonPink.copy(alpha = 0.45f),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerX, 0f),
                style = Stroke(width = 2.dp.toPx())
            )
            centerX += radius * 1.7f
        }
    }
}

private data class TagVisuals(
    val container: Color,
    val content: Color,
    val dot: Color
)
