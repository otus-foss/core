package com.otus.designsystem.theme.motion

import androidx.compose.animation.core.Easing
import androidx.compose.ui.util.lerp

/** A value at [at], a fraction of the whole animation. */
internal data class Keyframe(
    val at: Float,
    val value: Float,
)

internal infix fun Float.at(fraction: Float): Keyframe = Keyframe(fraction, this)

/**
 * One animated value over a timeline, like a CSS keyframes rule: [curve] eases each step between
 * neighbouring keyframes, and the value holds before the first keyframe and after the last.
 */
internal class KeyframeTrack(
    private val curve: MotionCurve,
    vararg keyframes: Keyframe,
) {
    private val steps = keyframes.toList().zipWithNext()

    fun valueAt(fraction: Float): Float =
        steps
            .firstOrNull { (_, to) -> fraction <= to.at }
            ?.let { (from, to) -> lerp(from.value, to.value, curve.easing.progress(fraction, from.at, to.at)) }
            ?: steps.last().second.value
}

private fun Easing.progress(
    fraction: Float,
    start: Float,
    end: Float,
): Float = transform(((fraction - start) / (end - start)).coerceIn(0f, 1f))
