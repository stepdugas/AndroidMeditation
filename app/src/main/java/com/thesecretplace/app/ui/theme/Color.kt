package com.thesecretplace.app.ui.theme

import androidx.compose.ui.graphics.Color

// Core palette — exact match of AppTheme.swift
val Background = Color(0xFF1A1F35)     // Deep midnight navy
val Surface = Color(0xFF232943)         // Elevated surface for cards
val Accent = Color(0xFFC9A84C)          // Muted warm gold
val Lavender = Color(0xFFB8A9D4)        // Soft lavender
val Cream = Color(0xFFF5F0E8)           // Warm ivory/cream
val DividerColor = Lavender.copy(alpha = 0.18f)

// Background gradient stops
val GradientTop = Color(0xFF1A1F35)     // Deep navy
val GradientMid = Color(0xFF231E38)     // Slightly purple midnight
val GradientBottom = Color(0xFF1E1932)  // Deep purple-navy

// Category colors — exact match of AppTheme.color(for:)
val CategoryMorning = Color(0xFFFFCC59)        // Warm amber
val CategorySleep = Color(0xFF8CB3FF)          // Periwinkle
val CategoryStressRelief = Color(0xFF73D9A6)   // Sage green
val CategoryBreathwork = Color(0xFF66D9F2)     // Sky blue
val CategorySoundscapes = Color(0xFFA6E6BF)    // Seafoam
val CategoryMeditatio = Color(0xFFFF99BF)      // Rose
val CategoryMentalTraining = Color(0xFFBFA6FF) // Violet

// Misc
val ErrorRed = Color(0xFFF26161)
val FlameTop = Color(0xFFFFE04D)
val FlameBottom = Color(0xFFFF731A)
