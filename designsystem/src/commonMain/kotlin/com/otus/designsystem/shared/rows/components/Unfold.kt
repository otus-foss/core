package com.otus.designsystem.shared.rows.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.shared.reorder.components.HALF
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlin.math.roundToInt

/**
 * How far a strip has unfolded into its open form: [fraction] 0 is folded and 1 open. A finger
 * moves it with [dragBy] as it drags, and [settle] or [animateTo] carry it the rest of the way on
 * the Otus easing. The unfolding layout sets how many pixels a full unfold travels.
 */
@Stable
public class Unfold internal constructor(
    open: Boolean,
    private val motionScale: Float,
) {
    public var fraction: Float by mutableFloatStateOf(if (open) 1f else 0f)
        private set

    /** Whether it rests between folded and open, as during a drag. */
    public val moving: Boolean
        get() = fraction > 0f && fraction < 1f

    // Written during layout, which must not write snapshot state; only a drag reads it.
    internal var range: Int = 0
    private var animation: Job? = null

    /** Follows a finger that moved [dy] pixels, up when negative, which unfolds. */
    public fun dragBy(dy: Float) {
        animation?.cancel()
        // Before the first layout the range is unknown; a guess keeps the first moves visible.
        val travel = range.takeIf { it > 0 } ?: GUESSED_RANGE
        fraction = (fraction - dy / travel).coerceIn(0f, 1f)
    }

    /**
     * After a drag let go at [velocity] pixels a second: an upward [flick] opens, a downward one
     * folds, and a slower release goes to the nearer end. Returns whether it ended open.
     */
    public suspend fun settle(
        velocity: Float,
        flick: Float,
    ): Boolean {
        val open =
            when {
                velocity <= -flick -> true
                velocity >= flick -> false
                else -> fraction >= HALF
            }
        animateTo(open)
        return open
    }

    /** Unfolds or folds the rest of the way, decelerating into place. */
    public suspend fun animateTo(open: Boolean) {
        val target = if (open) 1f else 0f
        if (fraction == target) return
        val job = currentCoroutineContext().job
        animation?.takeIf { it != job }?.cancel()
        animation = job
        try {
            val spec = Motion.tween<Float>(MotionSpeed.Normal, MotionCurve.Enter, motionScale)
            animate(fraction, target, animationSpec = spec) { value, _ -> fraction = value }
        } finally {
            if (animation == job) animation = null
        }
    }

    private companion object {
        const val HALF = 0.5f
        const val GUESSED_RANGE = 600
    }
}

/** An [Unfold] that starts [open], at the user's animation speed. */
@Composable
public fun rememberUnfold(open: Boolean): Unfold {
    val scale = LocalMotionScale.current
    return remember(scale) { Unfold(open, scale) }
}

/**
 * A strip card whose height follows [unfold] and nothing else, so its top edge stays with a
 * dragging finger: [folded] tall, as the strip it grows from, up to as tall as [content] when open,
 * growing upward from its bottom edge in between. [content] shows its bottom part while it grows,
 * so its top arrives last. The card itself stays sharp and opaque over the strip, while the
 * strip's own [foldedContent], drawn without its card, blurs away under [content] blurring in. It
 * is not shown folded, where the strip itself shows.
 */
@Composable
public fun UnfoldingStripCard(
    unfold: Unfold,
    folded: Dp,
    foldedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        {
            Box(Modifier.stripCardFrame()) {
                // Composed while folded, to measure the unfold, but hidden from screen readers.
                val hidden = if (unfold.fraction == 0f) Modifier.clearAndSetSemantics {} else Modifier
                Box(Modifier.unfolding(unfold, folded).unfoldedForm(unfold).then(hidden)) { content() }
                if (unfold.fraction < 1f) {
                    // The strip underneath is the one screen readers and taps reach.
                    Box(Modifier.align(Alignment.BottomStart).foldedForm(unfold).clearAndSetSemantics {}) {
                        CompositionLocalProvider(LocalStripFramed provides false, content = foldedContent)
                    }
                }
            }
        },
        modifier,
    ) { measurables, constraints ->
        // Measured even when folded, so the first drag already knows how far a full unfold travels.
        val card = measurables.single().measure(constraints)
        val shown = unfold.fraction > 0f
        layout(card.width, if (shown) card.height else 0) { if (shown) card.place(0, 0) }
    }
}

/** How opaque the open form of a strip is, reaching full well before the unfold does. */
internal val Unfold.fadeIn: Float
    get() = (fraction * CROSSFADE).coerceAtMost(1f)

/**
 * The open form of a strip as [unfold] grows it out of the folded form: it fades in, staying sharp
 * so its rows and lines never smear into the card's edge.
 */
public fun Modifier.unfoldedForm(unfold: Unfold): Modifier = graphicsLayer { alpha = unfold.fadeIn }

/**
 * The folded form of a strip, which blurs lightly and fades away as [unfold] grows the open form
 * over it, as the leaving part of a [BarMorph][com.otus.designsystem.shared.actionbar.components.BarMorph] does, so
 * the two never show sharp text together.
 */
public fun Modifier.foldedForm(unfold: Unfold): Modifier =
    graphicsLayer {
        val gone = unfold.fadeIn
        alpha = 1f - gone
        val radius = FOLDED_BLUR.toPx() * gone
        renderEffect = if (radius > 0f) BlurEffect(radius, radius, TileMode.Clamp) else null
    }

/**
 * How far the folded form blurs at most, half a
 * [BarMorph][com.otus.designsystem.shared.actionbar.components.BarMorph]'s, since it lasts only the first moves.
 */
private val FOLDED_BLUR = 3.dp

/** How much faster than the unfold the two forms cross-fade: over the first quarter of the travel. */
private const val CROSSFADE = 4f

/**
 * Lays its content out at full height but shows only the bottom part [unfold] has reached, from
 * [folded] tall up to all of it.
 */
internal fun Modifier.unfolding(
    unfold: Unfold,
    folded: Dp,
): Modifier =
    clipToBounds().layout { measurable, constraints ->
        val full = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
        val start = folded.roundToPx().coerceAtMost(full.height)
        unfold.range = full.height - start
        val height = (start + unfold.range * unfold.fraction).roundToInt().coerceAtMost(constraints.maxHeight)
        layout(full.width, height) { full.place(0, height - full.height) }
    }
