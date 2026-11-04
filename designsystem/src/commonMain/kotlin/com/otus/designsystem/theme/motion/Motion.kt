package com.otus.designsystem.theme.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
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

    /** Leaps off at once and settles slowly, for the brand mark rising into place at launch. */
    Rise(CubicBezierEasing(0.2f, 0.8f, 0.25f, 1f)),

    /** Overshoots and springs back, for the sidebar owl's hop. */
    Hop(CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)),
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

/** The user's choice of animation speed, provided as [LocalMotionScale]. [Instant] skips animations. */
public enum class AnimationSpeed(
    public val scale: Float,
) {
    Instant(Float.POSITIVE_INFINITY),
    Slow(0.75f),
    Regular(1f),
    Fast(1.5f),
}

/** Durations and animation specs for every Otus animation. */
public object Motion {
    public const val BASE_MILLIS: Int = 240

    /** Mail's launch animation as it first plays. Its keyframes are fractions of this length. */
    public const val LAUNCH_MILLIS: Int = 3000

    /** One round of the launch animation's loop, which repeats while the app loads. */
    public const val LAUNCH_LOOP_MILLIS: Int = 4200

    /** One round of the sidebar owl's idle animation: a glance aside, then a tilt. */
    public const val OWL_IDLE_MILLIS: Int = 30_000

    /** The sidebar owl's hop and smile after a tap. */
    public const val OWL_SMILE_MILLIS: Int = 1400

    /** The duration of an animation at [speed], with the global [scale] applied. */
    public fun millis(
        speed: MotionSpeed,
        scale: Float = 1f,
    ): Int = scaled(BASE_MILLIS / speed.factor, scale)

    /** A designed length, such as part of [LAUNCH_MILLIS], with the global [scale] applied. */
    public fun scaled(
        millis: Float,
        scale: Float,
    ): Int {
        require(scale > 0f) { "Motion scale must be positive, was $scale" }
        return (millis / scale).roundToInt()
    }

    /**
     * How far, in dp per millisecond, a surface that grows or travels by a varying distance moves,
     * so a short menu opens as fast as a tall one rather than in the same time.
     */
    public const val PACE_DP_PER_MILLI: Float = 1.6f

    /**
     * A tween across [distance] at [PACE_DP_PER_MILLI], kept between [MotionSpeed.Quick] and
     * [MotionSpeed.Relaxed], with the global [scale] applied.
     */
    public fun <T> paced(
        distance: Dp,
        curve: MotionCurve = MotionCurve.Standard,
        scale: Float = 1f,
    ): TweenSpec<T> {
        val millis =
            (distance.value / PACE_DP_PER_MILLI).coerceIn(
                baseMillis(MotionSpeed.Quick),
                baseMillis(MotionSpeed.Relaxed),
            )
        return TweenSpec(durationMillis = scaled(millis, scale), easing = curve.easing)
    }

    private fun baseMillis(speed: MotionSpeed): Float = BASE_MILLIS / speed.factor

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
