package com.thesecretplace.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }

// Detect screen width category
@Composable
fun rememberScreenSize(): ScreenSize {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width < 400 -> ScreenSize.COMPACT
        width < 600 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

@Composable
fun screenWidthDp(): Int = LocalConfiguration.current.screenWidthDp

@Composable
fun screenHeightDp(): Int = LocalConfiguration.current.screenHeightDp

// Scale a dp value based on screen width (baseline 400dp)
@Composable
fun responsiveDp(base: Dp, min: Dp = base * 0.65f, max: Dp = base * 1.2f): Dp {
    val width = screenWidthDp()
    val scale = (width / 400f).coerceIn(0.65f, 1.2f)
    return (base * scale).coerceIn(min, max)
}

// Scale font size based on screen width
@Composable
fun responsiveSp(base: TextUnit, compact: TextUnit? = null): TextUnit {
    val size = rememberScreenSize()
    return when (size) {
        ScreenSize.COMPACT -> compact ?: (base * 0.8f)
        ScreenSize.MEDIUM -> base
        ScreenSize.EXPANDED -> base
    }
}

// Responsive horizontal padding
@Composable
fun responsiveHorizontalPadding(): Dp {
    val size = rememberScreenSize()
    return when (size) {
        ScreenSize.COMPACT -> 16.dp
        ScreenSize.MEDIUM -> 22.dp
        ScreenSize.EXPANDED -> 40.dp
    }
}

// Hero banner height based on screen
@Composable
fun heroHeight(): Dp {
    val height = screenHeightDp()
    return (height * 0.38f).dp.coerceIn(220.dp, 360.dp)
}

// Timer ring size
@Composable
fun timerRingSize(): Dp {
    val width = screenWidthDp()
    return (width * 0.55f).dp.coerceIn(170.dp, 260.dp)
}

// Player progress ring size
@Composable
fun playerRingSize(): Dp {
    val width = screenWidthDp()
    return (width * 0.28f).dp.coerceIn(90.dp, 140.dp)
}

private operator fun TextUnit.times(factor: Float): TextUnit = (this.value * factor).sp
private operator fun Dp.times(factor: Float): Dp = (this.value * factor).dp
