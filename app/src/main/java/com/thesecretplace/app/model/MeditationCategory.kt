package com.thesecretplace.app.model

import androidx.compose.ui.graphics.Color
import com.thesecretplace.app.ui.theme.*

enum class MeditationCategory(val displayName: String) {
    MORNING("Morning"),
    BREATHWORK("Breathwork"),
    STRESS_RELIEF("Stress Relief"),
    SOUNDSCAPES("Soundscapes"),
    SLEEP("Sleep"),
    MEDITATIO("Meditatio"),
    MENTAL_TRAINING("Mental Training");

    val color: Color
        get() = when (this) {
            MORNING -> CategoryMorning
            SLEEP -> CategorySleep
            STRESS_RELIEF -> CategoryStressRelief
            BREATHWORK -> CategoryBreathwork
            SOUNDSCAPES -> CategorySoundscapes
            MEDITATIO -> CategoryMeditatio
            MENTAL_TRAINING -> CategoryMentalTraining
        }

    // Material icon names (using Material Icons Extended)
    val iconName: String
        get() = when (this) {
            MORNING -> "WbSunny"
            SLEEP -> "NightsStay"
            STRESS_RELIEF -> "Spa"
            BREATHWORK -> "Air"
            SOUNDSCAPES -> "GraphicEq"
            MEDITATIO -> "FavoriteBorder"
            MENTAL_TRAINING -> "Psychology"
        }

    companion object {
        fun fromRawValue(raw: String): MeditationCategory? {
            return entries.find { it.displayName == raw }
        }
    }
}
