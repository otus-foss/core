package com.otus.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.roundToInt

/**
 * The shape of an animation over time. [Standard] suits anything that changes in place, and
 * [Reverse] undoes such a change. [Enter] and [Exit] are for things that arrive or leave in a
 * direction, such as sheets, the sidebar, and replaced text.
 */
public enum class MotionCurve(
    public val easing: Easing,
) {
    Standard(CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),

    /** [Standard] played backward, so undoing a change retraces it frame for frame. */
    Reverse(CubicBezierEasing(0.8f, 0f, 0.6f, 1f)),

    /** Decelerates, so arriving content settles gently. */
    Enter(CubicBezierEasing(0.2f, 0f, 0f, 1f)),

    /** Accelerates, so leaving content gets out of the way. */
    Exit(CubicBezierEasing(0.4f, 0f, 1f, 1f)),
}

/** How fast an animation runs, relative to [Motion.BASE_MILLIS]. A higher [factor] is shorter. */
public enum class MotionSpeed(
    public val factor: Float,
) {
    /** 120 ms, for press feedback and small fades. */
    Quick(2f),

    /** 160 ms, for controls that change state and content that steps aside. */
    Fast(1.5f),

    /** 240 ms, the default for layout changes, sheets, and replaced text. */
    Normal(1f),

    /** 300 ms, for large surfaces that travel across the screen. */
    Relaxed(0.8f),
}

/**
 * Multiplies the speed of every Otus animation: 2 runs everything twice as fast. Android's system
 * animator duration scale still applies on top.
 */
public val LocalMotionScale: ProvidableCompositionLocal<Float> = staticCompositionLocalOf { 1f }

/** Durations and animation specs for every Otus animation. */
public object Motion {
    public const val BASE_MILLIS: Int = 240

    /** The duration of an animation at [speed], with the global [scale] applied. */
    public fun millis(
        speed: MotionSpeed,
        scale: Float = 1f,
    ): Int {
        require(scale > 0f) { "Motion scale must be positive, was $scale" }
        return (BASE_MILLIS / (speed.factor * scale)).roundToInt()
    }

    /** A tween for code outside composition, such as state holders. Pass [scale] from [LocalMotionScale]. */
    public fun <T> tween(
        speed: MotionSpeed = MotionSpeed.Normal,
        curve: MotionCurve = MotionCurve.Standard,
        scale: Float = 1f,
    ): TweenSpec<T> = TweenSpec(durationMillis = millis(speed, scale), easing = curve.easing)
}

/** A tween that applies [LocalMotionScale]. Read it outside non-composable lambdas such as transition specs. */
@Composable
@ReadOnlyComposable
public fun <T> motionTween(
    speed: MotionSpeed = MotionSpeed.Normal,
    curve: MotionCurve = MotionCurve.Standard,
): TweenSpec<T> = Motion.tween(speed, curve, LocalMotionScale.current)
