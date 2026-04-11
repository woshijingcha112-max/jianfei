package com.dietrecord.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = BlossomPink,
    secondary = CandyOrange,
    tertiary = StrawberryRed,
    background = CottonPink,
    surface = CreamWhite,
    surfaceVariant = PetalPink,
    onPrimary = CreamWhite,
    onSecondary = CreamWhite,
    onTertiary = CreamWhite,
    onBackground = PlumInk,
    onSurface = PlumInk,
    onSurfaceVariant = CocoaBrown,
    outline = RibbonPink,
    outlineVariant = RibbonPink.copy(alpha = 0.55f)
)

private val DarkScheme = darkColorScheme(
    primary = RibbonPink,
    secondary = CandyOrange,
    tertiary = StrawberryRed,
    background = PlumInk,
    surface = Color(0xFF38242D),
    surfaceVariant = Color(0xFF5A3946),
    onPrimary = PlumInk,
    onSecondary = PlumInk,
    onTertiary = CreamWhite,
    onBackground = CottonPink,
    onSurface = CottonPink,
    onSurfaceVariant = PetalPink,
    outline = RibbonPink.copy(alpha = 0.75f),
    outlineVariant = RibbonPink.copy(alpha = 0.4f)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun DietRecordTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
