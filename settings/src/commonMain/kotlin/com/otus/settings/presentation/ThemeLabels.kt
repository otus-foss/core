package com.otus.settings.presentation

import com.otus.designsystem.theme.AccentPreset
import com.otus.designsystem.theme.ThemeBase

/** The theme's name in settings, such as on the theme row. */
public val ThemeBase.title: String
    get() =
        when (this) {
            ThemeBase.Otus -> "Otus"
            ThemeBase.Light -> "Light"
            ThemeBase.Graphite -> "Graphite"
            ThemeBase.Black -> "Black"
        }

internal val ThemeBase.description: String
    get() =
        when (this) {
            ThemeBase.Otus -> "Soft dark, the default"
            ThemeBase.Light -> "Bright pages"
            ThemeBase.Graphite -> "Warm grey dark"
            ThemeBase.Black -> "True black, for OLED"
        }

internal val AccentPreset.title: String
    get() =
        when (this) {
            AccentPreset.OtusBlue -> "Otus blue"
            AccentPreset.Sky -> "Sky"
            AccentPreset.Blue -> "Blue"
            AccentPreset.Violet -> "Violet"
            AccentPreset.Pink -> "Pink"
            AccentPreset.Orange -> "Orange"
            AccentPreset.Green -> "Green"
        }
