package com.otus.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fixed brand values. Unlike the palette, they never follow the user's theme. */
public object BrandTokens {
    /** Behind the launcher icon and the launch animation; Android's `otus_brand_background` matches it. */
    public val background: Color = Color(0xFF151515)
    public val launchMarkSize: Dp = 192.dp

    /** How small the launch mark shrinks as it fades out. */
    public const val LAUNCH_EXIT_SCALE: Float = 0.6f
}
