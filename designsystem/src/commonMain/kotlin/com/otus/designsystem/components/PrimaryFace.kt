package com.otus.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.motionTween
import org.jetbrains.compose.resources.DrawableResource

/** What a bar's primary shows: its symbol and label. */
internal data class PrimaryFace(
    val symbol: DrawableResource,
    val mirrorInRtl: Boolean,
    val label: String,
)

/**
 * The face of the frontmost bar's primary, which is [primary]'s while this bar is in front. A new
 * bar starts from the face in front, so its primary swaps in from the bar it covers or replaces.
 * The face changes when a page opens over another, closes back to the one beneath, a selection's
 * bar replaces a list's, or a primary changes in place.
 */
@Composable
internal fun rememberPrimaryFace(primary: BarAction): Transition<PrimaryFace> {
    val own = PrimaryFace(primary.symbol, primary.mirrorInRtl, primary.label)
    val slot = remember { FrontBars.Slot(own) }
    val state = remember { MutableTransitionState(FrontBars.shown ?: own) }
    SideEffect { slot.face = own }
    DisposableEffect(slot) {
        FrontBars.slots += slot
        onDispose { FrontBars.slots -= slot }
    }
    state.targetState = FrontBars.shown ?: own
    return rememberTransition(state, label = "Primary face")
}

/**
 * Draws [content] for the current face. When the face changes, the old content fades and scales
 * out on the exit curve while the new one fades and scales in on the enter curve, both quick.
 */
@Composable
internal fun Transition<PrimaryFace>.Swapping(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (PrimaryFace) -> Unit,
) {
    val arrive = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Enter)
    val leave = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Exit)
    AnimatedContent(
        modifier,
        transitionSpec = { (fadeIn(arrive) + scaleIn(arrive)) togetherWith (fadeOut(leave) + scaleOut(leave)) },
        contentAlignment = contentAlignment,
    ) { content(it) }
}

/**
 * Every bar on screen, in the order they appeared; the last one is in front. It is app-wide
 * because each page has its own [ActionBarHost], and a page can open over another.
 */
private object FrontBars {
    val slots = mutableStateListOf<Slot>()

    val shown: PrimaryFace?
        get() = slots.lastOrNull()?.face

    class Slot(
        face: PrimaryFace,
    ) {
        var face by mutableStateOf(face)
    }
}
