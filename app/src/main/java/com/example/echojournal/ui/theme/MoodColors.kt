package com.example.echojournal.ui.theme

import androidx.compose.ui.graphics.Color

object MoodColors {
    // Professional shades: moving away from pure black/white
    val HappyPrimary = Color(0xFFFFD54F)
    val HappySecondary = Color(0xFFFF8F00)
    
    val SadPrimary = Color(0xFF9FA8DA)
    val SadSecondary = Color(0xFF283593)
    
    val StressedPrimary = Color(0xFFEF9A9A)
    val StressedSecondary = Color(0xFFC62828)
    
    val NeutralPrimary = Color(0xFFB0BEC5)
    val NeutralSecondary = Color(0xFF37474F)

    // "Soft" dark background (Space Gray)
    val BackgroundTop = Color(0xFF1A1C1E)
    val BackgroundBottom = Color(0xFF0F1011)

    // Text colors (not pure white)
    val TextPrimary = Color(0xFFE3E2E6)
    val TextSecondary = Color(0xFF909094)

    fun getColorsForMood(mood: String): Pair<Color, Color> {
        return when (mood.lowercase()) {
            "happy" -> HappyPrimary to HappySecondary
            "sad" -> SadPrimary to SadSecondary
            "stressed" -> StressedPrimary to StressedSecondary
            else -> NeutralPrimary to NeutralSecondary
        }
    }
}
