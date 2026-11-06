package com.otus.designsystem.shared.settings.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * How far a [ThemePhonePreview] has grown from its lower half to the taller phone, from 0 to 1. A
 * tap runs the whole way with [toggle]; a drag follows the finger with [drag] and [settle] finishes it.
 */
@Stable
public class PhoneExpansion internal constructor(
    private val scope: CoroutineScope,
) {
    internal val progress = Animatable(0f)

    /** How many pixels the phone grows by, once it is measured. */
    internal var reach by mutableFloatStateOf(0f)
    internal var fling = 0f
    internal var density = 1f
    internal var motionScale = 1f

    /** Whether the phone is grown or on its way there. */
    public val expanded: Boolean
        get() = progress.targetValue > HALFWAY

    public fun toggle() {
        animateTo(if (expanded) 0f else 1f)
    }

    public fun collapse() {
        animateTo(0f)
    }

    internal fun drag(delta: Float) {
        if (reach <= 0f) return
        scope.launch { progress.snapTo((progress.value + delta / reach).coerceIn(0f, 1f)) }
    }

    internal fun settle(velocity: Float) {
        val grows = if (abs(velocity) > fling) velocity > 0f else progress.value > HALFWAY
        animateTo(if (grows) 1f else 0f)
    }

    private fun animateTo(target: Float) {
        val curve = if (target > progress.value) MotionCurve.Enter else MotionCurve.Exit
        scope.launch { progress.animateTo(target, paced(abs(target - progress.value), curve)) }
    }

    /** A tween across [share] of the growth, at one pace whatever the phone's size. */
    private fun paced(
        share: Float,
        curve: MotionCurve,
    ): AnimationSpec<Float> = Motion.paced((reach * share / density).dp, curve, motionScale)
}

@Composable
public fun rememberPhoneExpansion(): PhoneExpansion {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    return remember(scope) { PhoneExpansion(scope) }.apply {
        fling = with(density) { PanelTokens.sheetCloseFling.toPx() }
        this.density = density.density
        motionScale = LocalMotionScale.current
    }
}

/** A vertical drag grows the phone downward and shrinks it upward, in step with the finger. */
@Composable
internal fun Modifier.dragsPhone(expansion: PhoneExpansion): Modifier =
    draggable(
        rememberDraggableState(expansion::drag),
        Orientation.Vertical,
        onDragStopped = { expansion.settle(it) },
    )

private const val HALFWAY = 0.5f
