package com.otus.designsystem.shared.swipe.components

import androidx.compose.animation.core.animate
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed

/**
 * When a swipe that moved a surface toward closing closes it: once it moved [distance] pixels that
 * way, or was released faster than [fling] pixels a second that way. Sheets, panel sheets, and
 * toasts each pass their own direction and thresholds.
 */
internal class DismissRule(
    private val distance: Float,
    private val fling: Float = Float.POSITIVE_INFINITY,
) {
    /** Whether a release after moving [moved] pixels toward closing, at [velocity] that way, closes. */
    fun dismisses(
        moved: Float,
        velocity: Float,
    ): Boolean = moved > 0f && (moved >= distance || velocity > fling)
}

/** Moves a surface a swipe did not close from [from] back to rest at 0, reporting each frame. */
internal suspend fun settleBack(
    from: Float,
    motionScale: Float,
    onFrame: (Float) -> Unit,
) {
    animate(from, 0f, animationSpec = Motion.tween(MotionSpeed.Normal, MotionCurve.Standard, motionScale)) { value, _ ->
        onFrame(value)
    }
}
