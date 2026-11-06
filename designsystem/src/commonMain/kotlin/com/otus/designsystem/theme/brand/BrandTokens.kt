package com.otus.designsystem.theme.brand

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Sizes for brand marks. Their colors come from the theme. */
internal object BrandTokens {
    public val launchMarkSize: Dp = 192.dp

    /** A launcher icon's preview, such as in the App icon setting. */
    public val appIconSize: Dp = 72.dp

    /** The rounded square a preview's icon sits in, as most launchers shape app icons. */
    public val appIconRadius: Dp = 20.dp

    /** An adaptive icon's foreground is this much larger than the part a launcher shows. */
    public const val ADAPTIVE_ICON_BLEED: Float = 1.5f

    /** How small the launch mark shrinks as it fades out. */
    public const val LAUNCH_EXIT_SCALE: Float = 0.6f
}
