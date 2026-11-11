package com.otusfoss.core.designsystem.theme.brand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.otusfoss.core.designsystem.theme.colors.OtusColors

/** How the app icon and the launch animation color the owl. */
public enum class AppIconStyle {
    /** The owl in the accent color. */
    Accent,

    /** The owl in the default text color. */
    Monochrome,

    /** The owl as polished metal on a white tile. */
    Chrome,

    /** The owl in the rainbow pride flag. */
    Rainbow,

    /** The owl in the transgender pride flag. */
    Trans,

    /** The owl in the bisexual pride flag. */
    Bi,

    /** The owl in the lesbian pride flag. */
    Lesbian,
    ;

    /** The owl's color in the current theme; the launch animation draws flags and metal plainly. */
    public val markColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (this == Monochrome || this == Chrome) OtusColors.text1 else OtusColors.accentRegular
}
