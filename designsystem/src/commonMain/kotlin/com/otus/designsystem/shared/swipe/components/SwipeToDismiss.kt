package com.otus.designsystem.shared.swipe.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.feedback.ToastTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Lets the user drag [content] sideways in either direction to dismiss it, as a toast or the
 * Read view's images banner. A darker end opens behind it on the side it leaves, with a dim close
 * symbol and "Dismiss". Past [ToastTokens.DISMISS_FRACTION] of the width they brighten and a haptic
 * marks it; releasing there slides [content] off the screen while the end fades, then calls
 * [onDismiss]. Shorter drags spring back. [onHeld] reports whether it is away from rest, so a
 * timer can pause. [content] receives whether it is lifted.
 */
@Composable
public fun SwipeToDismiss(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onHeld: (Boolean) -> Unit = {},
    content: @Composable (lifted: Boolean) -> Unit,
) {
    var width by remember { mutableIntStateOf(0) }
    var offset by remember { mutableFloatStateOf(0f) }
    var settle by remember { mutableStateOf<Job?>(null) }
    val fade = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val motionScale = LocalMotionScale.current
    val window = LocalWindowInfo.current.containerSize.width
    val currentOnHeld by rememberUpdatedState(onHeld)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val armed = width > 0 && abs(offset) >= width * ToastTokens.DISMISS_FRACTION
    val held = offset != 0f
    ThresholdHaptic(armed)
    LaunchedEffect(held) { currentOnHeld(held) }
    Box(
        modifier
            .onSizeChanged { width = it.width }
            .draggable(
                rememberDraggableState { offset += it },
                Orientation.Horizontal,
                enabled,
                onDragStarted = { settle?.cancel() },
                onDragStopped = {
                    val dismissing = abs(offset) >= width * ToastTokens.DISMISS_FRACTION
                    settle =
                        scope.launch {
                            if (dismissing) {
                                launch {
                                    fade.animateTo(
                                        0f,
                                        Motion.tween(MotionSpeed.Normal, MotionCurve.Exit, motionScale),
                                    )
                                }
                                // A whole window's width carries it past the screen's edge from anywhere.
                                slide(offset, offset.sign * window, MotionCurve.Exit, motionScale) { offset = it }
                                currentOnDismiss()
                            } else {
                                slide(offset, 0f, MotionCurve.Standard, motionScale) { offset = it }
                            }
                        }
                },
            ),
    ) {
        // The toast slides away whole; only the end behind it fades as it goes.
        if (held) DismissEnd(offset, armed, fade.value, Modifier.matchParentSize())
        Box(Modifier.graphicsLayer { translationX = offset }) { content(held) }
    }
}

private suspend fun slide(
    from: Float,
    to: Float,
    curve: MotionCurve,
    motionScale: Float,
    onFrame: (Float) -> Unit,
) = animate(
    from,
    to,
    animationSpec = Motion.tween(MotionSpeed.Normal, curve, motionScale),
) { value, _ -> onFrame(value) }

/**
 * The darker pill behind a dragged toast, from the edge it left to just under it. It stretches
 * outward, away from the drag, by up to [ToastTokens.dismissStretch], so "Dismiss" shows sooner.
 * The close symbol and "Dismiss" sit at its outer edge and stay drawn; the toast covers what it
 * has not yet uncovered. They are dim until [armed]; then they brighten. All of it draws at
 * [alpha], which fades it while a dismissed toast slides away.
 */
@Composable
private fun DismissEnd(
    offset: Float,
    armed: Boolean,
    alpha: Float,
    modifier: Modifier,
) {
    val fromLeft = offset > 0f
    val stretch =
        with(LocalDensity.current) {
            (abs(offset) * ToastTokens.STRETCH_RATIO).coerceAtMost(ToastTokens.dismissStretch.toPx())
        }
    val color by animateColorAsState(
        if (armed) OtusColors.text1 else OtusColors.text3,
        motionTween(MotionSpeed.Quick),
        label = "Dismiss color",
    )
    // A layer would clip the stretch past the toast's edge, so the pill and its row fade on their own.
    Box(modifier.dismissPill(offset, stretch, alpha)) {
        Row(
            Modifier
                .align(if (fromLeft) AbsoluteAlignment.CenterLeft else AbsoluteAlignment.CenterRight)
                .graphicsLayer {
                    translationX = if (fromLeft) -stretch else stretch
                    this.alpha = alpha
                }.padding(horizontal = ToastTokens.textInset),
            horizontalArrangement = Arrangement.Absolute.spacedBy(ToastTokens.actionGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val label = @Composable { Text("Dismiss", style = panelTextStyle(), color = color, maxLines = 1) }
            if (!fromLeft) label()
            CompositionLocalProvider(LocalContentColor provides color) { Symbol(Res.drawable.symbol_close) }
            if (fromLeft) label()
        }
    }
}

/**
 * Fills a pill at [alpha] from the side the toast left, [stretch] past that edge. It always reaches half a
 * toast's height past [offset], to the middle of the toast's round end, so the two stay joined.
 * Only its outer end is round; the square inner end stays under the toast.
 */
private fun Modifier.dismissPill(
    offset: Float,
    stretch: Float,
    alpha: Float,
): Modifier =
    drawBehind {
        val fromLeft = offset > 0f
        val reach = abs(offset) + stretch + size.height / 2f
        val left = if (fromLeft) -stretch else size.width + stretch - reach
        val round = CornerRadius(size.height / 2f)
        val border = PanelTokens.borderWidth.toPx()

        fun pill(inset: Float) =
            Path().apply {
                val rect = Rect(left + inset, inset, left + reach - inset, size.height - inset)
                val outer = CornerRadius(round.x - inset)
                addRoundRect(
                    if (fromLeft) {
                        RoundRect(rect, topLeft = outer, bottomLeft = outer)
                    } else {
                        RoundRect(rect, topRight = outer, bottomRight = outer)
                    },
                )
            }
        drawPath(pill(0f), OtusColors.bgDark, alpha)
        drawPath(pill(border / 2f), OtusColors.strokeBgDark, alpha, Stroke(border))
    }
