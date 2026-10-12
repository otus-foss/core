package com.otus.designsystem.theme

import androidx.compose.ui.graphics.Color

/** The accent colors offered before a custom one. [OtusBlue] is the default. */
public enum class AccentPreset(
    public val color: Color,
) {
    OtusBlue(Color(0xFF4D6F92)),
    Sky(Color(0xFF1D9BF0)),
    Blue(Color(0xFF2383E2)),
    Violet(Color(0xFF7C5CC4)),
    Pink(Color(0xFFC05A8F)),
    Orange(Color(0xFFC8613A)),
    Green(Color(0xFF3F8F6B)),
    ;

    public companion object {
        /** The preset whose color is [color], or null for a custom color. */
        public fun of(color: Color): AccentPreset? = entries.firstOrNull { it.color == color }
    }
}
