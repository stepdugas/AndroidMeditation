package com.thesecretplace.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Full-screen vertical gradient: deep navy → soft purple/midnight
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(GradientTop, GradientMid, GradientBottom)
)

// Subtle warm radial glow placed at top of screen
fun warmRadialGlow(opacity: Float = 0.20f) = Brush.radialGradient(
    colors = listOf(Accent.copy(alpha = opacity), Color.Transparent),
    center = Offset(0.5f, 0f),
    radius = 1200f
)

// Lavender radial glow at bottom
fun lavenderRadialGlow(opacity: Float = 0.14f) = Brush.radialGradient(
    colors = listOf(Lavender.copy(alpha = opacity), Color.Transparent),
    center = Offset(0.5f, 1f),
    radius = 1100f
)

// Cinematic bottom-to-top vignette for hero images
val HeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.85f),
        Color.Black.copy(alpha = 0.35f),
        Color.Transparent
    ),
    startY = Float.POSITIVE_INFINITY,
    endY = 0f
)

// Dual vignette (top + bottom) for player full-screen
val PlayerVignette = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.60f),
        Color.Black.copy(alpha = 0.15f),
        Color.Black.copy(alpha = 0.70f)
    )
)
