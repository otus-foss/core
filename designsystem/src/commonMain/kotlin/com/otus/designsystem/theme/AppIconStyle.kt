package com.otus.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** How the app icon and the launch animation color the owl. */
public enum class AppIconStyle {
    /** The owl in the accent color. */
    Accent,

    /** The owl in the default text color. */
    Monochrome,
    ;

    /** The owl's color in the current theme. */
    public val markColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (this == Accent) OtusColors.accentRegular else OtusColors.text1
}
