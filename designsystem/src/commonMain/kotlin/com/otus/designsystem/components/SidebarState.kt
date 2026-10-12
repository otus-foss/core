package com.otus.designsystem.components

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Whether the [Sidebar] is open, and how far it currently shows. Buttons call [open] and [close];
 * swipes move it directly and then settle on the nearer side, or on the side a fling points to.
 */
@Stable
public class SidebarState(
    initiallyOpen: Boolean = false,
) {
    /** The side the sidebar is at or heading to. */
    public var isOpen: Boolean by mutableStateOf(initiallyOpen)
        private set

    /** 0 when hidden, 1 when fully shown. */
    public var progress: Float by mutableFloatStateOf(if (initiallyOpen) 1f else 0f)
        private set

    /** Panel width in pixels, which converts a swipe distance into progress. */
    internal var widthPx by mutableFloatStateOf(0f)

    /** The [com.otus.designsystem.theme.LocalMotionScale] of the [Sidebar] that shows this state. */
    internal var motionScale = 1f

    private val mutex = MutatorMutex()

    public suspend fun open(): Unit = animateTo(open = true)

    public suspend fun close(): Unit = animateTo(open = false)

    /** Stops a running animation so a swipe can take over. */
    internal suspend fun stop() = mutex.mutate(MutatePriority.UserInput) {}

    /** [delta] is the movement toward the open side, in pixels. */
    internal fun dragBy(delta: Float) {
        if (widthPx > 0f) progress = (progress + delta / widthPx).coerceIn(0f, 1f)
    }

    /** [velocity] is the release speed toward the open side, in pixels per second. */
    internal suspend fun settle(
        velocity: Float,
        flingThreshold: Float,
    ) {
        val open =
            when {
                velocity > flingThreshold -> true
                velocity < -flingThreshold -> false
                else -> progress > HALFWAY
            }
        animateTo(open)
    }

    private suspend fun animateTo(open: Boolean) =
        mutex.mutate {
            isOpen = open
            val target = if (open) 1f else 0f
            // A sidebar released part-way only travels the remaining distance, at the same pace.
            val remaining = abs(target - progress)
            val millis = Motion.millis(if (open) MotionSpeed.Relaxed else MotionSpeed.Normal, motionScale)
            val curve = if (open) MotionCurve.Enter else MotionCurve.Exit
            val spec = TweenSpec<Float>((millis * remaining).roundToInt(), easing = curve.easing)
            animate(progress, target, animationSpec = spec) { value, _ -> progress = value }
        }

    public companion object {
        private const val HALFWAY = 0.5f

        public val Saver: Saver<SidebarState, Boolean> = Saver(save = { it.isOpen }, restore = { SidebarState(it) })
    }
}

@Composable
public fun rememberSidebarState(): SidebarState = rememberSaveable(saver = SidebarState.Saver) { SidebarState() }
