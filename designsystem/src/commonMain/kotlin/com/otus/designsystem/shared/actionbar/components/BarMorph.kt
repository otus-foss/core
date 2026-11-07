package com.otus.designsystem.shared.actionbar.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Shows the bar [content] draws for [shown], such as a formatting row or a full [ActionBar], and
 * morphs from one to
 * the next when [shown] changes. Each part first takes its match's height, then its width, so a
 * row splits into a group and shell, or they merge into a row. While the width changes, the
 * parts' content blurs and the arriving content fades in over the leaving one. A bar without a
 * group or a shell, such as a minimized one, fades instead.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
public fun <T> BarMorph(
    shown: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val morph = Morph(LocalMotionScale.current)
    SharedTransitionLayout(modifier) {
        AnimatedContent(
            shown,
            transitionSpec = {
                fadeIn(morph.crossfade) togetherWith fadeOut(morph.crossfade) using SizeTransform(clip = false)
            },
            label = "Bar morph",
        ) { bar ->
            val parts = BarParts(group = part("group", morph, this), primary = part("primary", morph, this))
            CompositionLocalProvider(LocalBarParts provides parts) { content(bar) }
        }
    }
}

/** Ties a part to its match under [key], and blurs it while the [morph] changes its width. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.part(
    key: String,
    morph: Morph,
    scope: AnimatedVisibilityScope,
): Modifier {
    val arriving = scope.transition.targetState == EnterExitState.Visible
    // 0 to 1 over the morph for both parts: the arriving part runs from PreEnter to Visible.
    val shown by scope.transition.animateFloat({ tween(morph.millis, easing = LinearEasing) }, "Bar part") {
        if (it == EnterExitState.Visible) 1f else 0f
    }
    return Modifier
        .sharedBounds(
            rememberSharedContentState(key),
            scope,
            enter = fadeIn(morph.fade),
            // The leaving part stays whole under the arriving one, so the page never shows through.
            exit = fadeOut(snap(delayMillis = morph.millis)),
            boundsTransform = morph.bounds,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            zIndexInOverlay = if (arriving) 1f else 0f,
        ).graphicsLayer {
            val radius = BLUR_RADIUS.toPx() * morph.blurShare(if (arriving) shown else 1f - shown)
            renderEffect = if (radius > 0f) BlurEffect(radius, radius, TileMode.Clamp) else null
            // The rounder bar's corners lie inside the less round clip, so neither bar loses an edge.
            shape = SmoothCornerShape(minOf(ActionBarTokens.formatBarRadius, ActionBarTokens.radius))
            clip = true
        }
}

/** The steps of one morph at the motion [scale], lasting [millis]: the height, then the width. */
@OptIn(ExperimentalSharedTransitionApi::class)
private class Morph(
    scale: Float,
) {
    val millis = Motion.millis(MotionSpeed.Normal, scale)
    private val heightMillis = (millis * HEIGHT_SHARE).roundToInt()

    /** Bars without parts cross-fade over the whole morph, as the parts change in place. */
    val crossfade: FiniteAnimationSpec<Float> = Motion.tween(MotionSpeed.Normal, MotionCurve.Standard, scale)

    /** The arriving part fades in while the width changes, on the curve its bounds follow. */
    val fade: FiniteAnimationSpec<Float> =
        tween(millis - heightMillis, delayMillis = heightMillis, easing = MotionCurve.Standard.easing)

    /** Through the starting bounds at the target's height. */
    val bounds =
        BoundsTransform { initial, target ->
            if (millis == 0) {
                snap()
            } else {
                val easing = MotionCurve.Standard.easing
                keyframes {
                    durationMillis = millis
                    initial at 0 using easing
                    Rect(initial.left, target.top, initial.right, target.bottom) at heightMillis using easing
                }
            }
        }

    /** How blurred a part is at [progress] through the morph: clear, then most blurred halfway through the width. */
    fun blurShare(progress: Float): Float {
        val width = ((progress - HEIGHT_SHARE) / (1f - HEIGHT_SHARE)).coerceIn(0f, 1f)
        return sin(width * PI.toFloat())
    }

    private companion object {
        /** The share of the morph spent on the height before the width changes. */
        const val HEIGHT_SHARE = 0.3f
    }
}

/** How far a part's content blurs at most, halfway through a [BarMorph]'s width change. */
private val BLUR_RADIUS = 6.dp
