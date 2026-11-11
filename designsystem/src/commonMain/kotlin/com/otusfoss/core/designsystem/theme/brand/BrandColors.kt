package com.otusfoss.core.designsystem.theme.brand

import androidx.compose.ui.graphics.Color

/** Colors of Mail's launcher icons, which never follow the theme. */
internal object BrandColors {
    /** Behind every launcher icon but Chrome; matches `otus_brand_background`. */
    public val iconBackground: Color = Color(0xFF151515)

    /** The chrome icon's white tile, from top to bottom, matching its launcher icon. */
    public val chromeTileTop: Color = Color(0xFFFFFFFF)
    public val chromeTileBottom: Color = Color(0xFFE2E4E8)
}
