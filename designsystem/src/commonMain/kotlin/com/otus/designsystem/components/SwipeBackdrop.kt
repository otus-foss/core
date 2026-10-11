package com.otus.designsystem.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.MotionTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SwipeTokens
import kotlin.math.abs

/**
 * The accent area a swiped row uncovers, [reveal] wide, shaped by [swipeReveal]. The action's icon
 * and label stay anchored at the revealed edge and are clipped to it. The icon grows and slides in
 * until the drag [distance] reaches the threshold. [onMeasured] reports the width the label needs.
 */
@Composable
internal fun SwipeBackdrop(
    action: SwipeAction,
    distance: Float,
    reveal: Float,
    fromStart: Boolean,
    flash: Float,
    modifier: Modifier,
    onMeasured: (Float) -> Unit,
) {
    val density = LocalDensity.current
    val fromLeft = revealFromLeft(fromStart, LocalLayoutDirection.current == LayoutDirection.Ltr)
    val progress = swipeProgress(distance, with(density) { SwipeTokens.threshold.toPx() })
    val scale = SwipeTokens.ICON_START_SCALE + progress * (1f - SwipeTokens.ICON_START_SCALE)
    val shift = with(density) { SwipeTokens.iconEntry.toPx() } * (1f - progress) * if (fromLeft) -1f else 1f
    val fillet = with(density) { SwipeTokens.filletRadius.toPx() }
    val corner = with(density) { SwipeTokens.cornerRadius.toPx() }
    Box(modifier.revealArea(reveal, fromLeft, fillet, corner, flash)) {
        Row(
            Modifier
                .align(if (fromStart) Alignment.CenterStart else Alignment.CenterEnd)
                .fillMaxHeight()
                .onSizeChanged { onMeasured(it.width.toFloat()) }
                .padding(horizontal = SwipeTokens.inset),
            horizontalArrangement = Arrangement.spacedBy(SwipeTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!fromStart) Text(action.label, color = OtusColors.text1, maxLines = 1)
            Box(
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = shift
                },
            ) { Symbol(action.icon) }
            if (fromStart) Text(action.label, color = OtusColors.text1, maxLines = 1)
        }
    }
}

/**
 * Fills the uncovered area with the accent, brightened by [flash] when a release commits, and
 * draws the content only inside it.
 */
private fun Modifier.revealArea(
    reveal: Float,
    fromLeft: Boolean,
    fillet: Float,
    corner: Float,
    flash: Float,
): Modifier =
    drawWithCache {
        val area = Path()
        area.swipeReveal(size, reveal, fromLeft, revealRadii(reveal, size.height, fillet, corner))
        onDrawWithContent {
            drawPath(area, OtusColors.accentRegular)
            if (flash > 0f) drawPath(area, OtusColors.text1.copy(alpha = flash))
            clipPath(area) { this@onDrawWithContent.drawContent() }
        }
    }

/**
 * Settles a release by distance alone: past [threshold] it commits, reporting the side through
 * [onCommit] before the row reaches its anchor; anything shorter returns to rest.
 */
internal fun thresholdFlingBehavior(
    state: AnchoredDraggableState<SwipeAnchor>,
    threshold: Float,
    onCommit: (SwipeAnchor) -> Unit,
): TargetedFlingBehavior =
    object : TargetedFlingBehavior {
        override suspend fun ScrollScope.performFling(
            initialVelocity: Float,
            onRemainingDistanceUpdated: (Float) -> Unit,
        ): Float {
            val start = state.requireOffset()
            val target = swipeTarget(start, threshold, state.anchors)
            if (target != SwipeAnchor.Settled) onCommit(target)
            val end = state.anchors.positionOf(target)
            var previous = start
            onRemainingDistanceUpdated(end - start)
            animate(
                start,
                end,
                animationSpec = tween(SwipeTokens.SETTLE_MILLIS, easing = MotionTokens.standard),
            ) { value, _ ->
                previous += scrollBy(value - previous)
                onRemainingDistanceUpdated(end - previous)
            }
            return 0f
        }
    }

internal enum class SwipeAnchor { EndToStart, Settled, StartToEnd }

internal fun SwipeAnchor?.pick(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
): SwipeAction? =
    when (this) {
        SwipeAnchor.StartToEnd -> startToEnd
        SwipeAnchor.EndToStart -> endToStart
        SwipeAnchor.Settled, null -> null
    }

/** The side a row at [offset] is revealing, measured toward the end edge. */
internal fun directionOf(offset: Float): SwipeAnchor =
    when {
        offset > 0f -> SwipeAnchor.StartToEnd
        offset < 0f -> SwipeAnchor.EndToStart
        else -> SwipeAnchor.Settled
    }

/** Where a release at [offset] settles: a side only when it is past [threshold] and has an action. */
internal fun swipeTarget(
    offset: Float,
    threshold: Float,
    anchors: DraggableAnchors<SwipeAnchor>,
): SwipeAnchor =
    when {
        offset >= threshold && anchors.hasPositionFor(SwipeAnchor.StartToEnd) -> SwipeAnchor.StartToEnd
        offset <= -threshold && anchors.hasPositionFor(SwipeAnchor.EndToStart) -> SwipeAnchor.EndToStart
        else -> SwipeAnchor.Settled
    }

/** Whether a side's action is revealed from the physical left edge. */
internal fun revealFromLeft(
    fromStart: Boolean,
    isLtr: Boolean,
): Boolean = fromStart == isLtr

internal fun swipeProgress(
    offset: Float,
    distance: Float,
): Float = (abs(offset) / distance).coerceIn(0f, 1f)

/** The row's own offset, which stops at [maxDistance] while the drag continues. */
internal fun boundedRevealOffset(
    offset: Float,
    maxDistance: Float,
): Float = offset.coerceIn(-maxDistance, maxDistance)

/** The corner radii a revealed area can use at its current size. */
internal data class RevealRadii(
    /** Concave radius where the area meets the row's edge, reaching this far past the row. */
    val fillet: Float,
    /** Convex radius of the body's free end, next to the moving row. */
    val corner: Float,
)

/**
 * Shrinks [fillet] and [corner] to fit a revealed area [width] wide in a row [height] tall, so a
 * narrow area starts as a sliver at the edge and grows into the full shape.
 */
internal fun revealRadii(
    width: Float,
    height: Float,
    fillet: Float,
    corner: Float,
): RevealRadii {
    val edge = minOf(fillet, width).coerceAtLeast(0f)
    val end = minOf(corner, width - edge, height / 2f).coerceAtLeast(0f)
    return RevealRadii(edge, end)
}
