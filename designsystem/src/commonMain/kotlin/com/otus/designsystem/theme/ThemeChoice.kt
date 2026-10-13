package com.otus.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** A theme and the one accent color the user picked; [palette] derives every color from them. */
@Immutable
public data class ThemeChoice(
    public val base: ThemeBase = ThemeBase.Otus,
    public val accent: Color = AccentPreset.OtusBlue.color,
) {
    /** The accent's fill, outline, and text shades for [base]. */
    public val shades: AccentShades
        get() = base.accentShades(accent)

    public val palette: OtusPalette
        get() {
            val accentShades = shades
            return OtusPalette(
                accentLight = accentShades.text,
                accentRegular = accentShades.regular,
                accentDark = accentShades.fill,
                bgLight = base.light,
                bgRegular = base.page,
                bgDark = base.raised,
                text1 = base.text1,
                text2 = base.text2,
                text3 = base.text3,
                strokeBgDark = base.stroke,
                isLight = base.isLight,
            )
        }
}
