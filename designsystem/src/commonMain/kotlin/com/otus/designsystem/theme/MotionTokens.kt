package com.otus.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing

/** Suite motion defaults. Keep these values aligned in Mail and Calendar. */
public object MotionTokens {
    public val standard: CubicBezierEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    public val enter: CubicBezierEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    public val exit: CubicBezierEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    public const val TEXT_REPLACEMENT_MILLIS: Int = 260
    public const val TEXT_EXIT_MILLIS: Int = 180
}
