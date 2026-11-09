package com.otus.designsystem.shared.swipe.components

import androidx.compose.animation.core.animate
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.tokens.rows.EntryTokens
import com.otus.designsystem.theme.tokens.rows.SwipeTokens
import kotlin.math.abs

/**
 * The accent band behind a swiped row, edge to edge at the row's height, which the row slides off.
 * Where it meets the uncovered screen edge, inverted corners flare it up and down around the
 * neighboring cards. The action's icon and label sit [edgeInset] in from the band's edge, where the row's card begins.
 * The icon grows and slides in until the drag [distance] reaches the threshold. A release that
 * commits brightens the band by [flash]. [onMeasured] reports the width the label needs.
 */
@Composable
internal fun SwipeBackdrop(
    action: SwipeAction,
    distance: Float,
    fromStart: Boolean,
    flash: Float,
    edgeInset: Dp,
    modifier: Modifier,
    onMeasured: (Float) -> Unit,
) {
    val density = LocalDensity.current
    val fromLeft = revealFromLeft(fromStart, LocalLayoutDirection.current == LayoutDirection.Ltr)
    val progress = swipeProgress(distance, with(density) { SwipeTokens.threshold.toPx() })
    val scale = SwipeTokens.ICON_START_SCALE + progress * (1f - SwipeTokens.ICON_START_SCALE)
    val shift = with(density) { SwipeTokens.iconEntry.toPx() } * (1f - progress) * if (fromLeft) -1f else 1f
    Box(
        modifier.edgeBand(
            OtusColors.text1.copy(alpha = flash).compositeOver(OtusColors.accentDark),
            edgeInset,
            fromLeft,
        ),
    ) {
        Row(
            Modifier
                .align(if (fromStart) Alignment.CenterStart else Alignment.CenterEnd)
                .fillMaxHeight()
                // Outside the measurement, so the row still stops just past the label.
                .padding(horizontal = edgeInset)
                .onSizeChanged { onMeasured(it.width.toFloat()) }
                .padding(horizontal = SwipeTokens.inset),
            horizontalArrangement = Arrangement.spacedBy(SwipeTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!fromStart) Text(action.action.label, color = OtusColors.text1, maxLines = 1)
            Box(
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = shift
                },
            ) { Symbol(action.symbol) }
            if (fromStart) Text(action.action.label, color = OtusColors.text1, maxLines = 1)
        }
    }
}

/**
 * Fills the band with [color] and, at the left or right screen edge, flares it around the cards
 * above and below. Each flare is an inverted corner that follows the neighbor card's corner at the
 * list's gap, so the band keeps the same distance to every card. The row's card sits [edgeInset]
 * in from the edge; the flares draw outside the band's bounds.
 */
private fun Modifier.edgeBand(
    color: Color,
    edgeInset: Dp,
    fromLeft: Boolean,
): Modifier =
    drawBehind {
        drawRect(color)
        val flare = edgeFlare(edgeInset.toPx(), EntryTokens.gap.toPx(), EntryTokens.radius.toPx()) ?: return@drawBehind
        listOf(1f, -1f).forEach { vertical ->
            scale(if (fromLeft) 1f else -1f, vertical) { drawPath(flare, color) }
        }
    }

/**
 * The flare above a band's left edge, in the band's coordinates: a strip up the screen edge that
 * stops [gap] short of the card above, then curves around that card's [radius] corner, [gap] away,
 * back to the band's top. Null when the inset leaves no room beside the cards.
 */
internal fun edgeFlare(
    inset: Float,
    gap: Float,
    radius: Float,
): Path? {
    if (inset <= gap) return null
    val top = -(gap + radius)
    return Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, top)
        lineTo(inset - gap, top)
        arcTo(
            Rect(Offset(inset + radius, top), radius + gap),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false,
        )
        close()
    }
}

/**
 * Settles a release by distance alone. Past [threshold], [commit] takes over the row and moves it
 * from where it was released; anything shorter, or a side [commit] declines, returns to rest at a
 * pace set by [motionScale].
 */
internal fun thresholdFlingBehavior(
    state: AnchoredDraggableState<SwipeAnchor>,
    threshold: Float,
    motionScale: Float,
    commit: (SwipeAnchor) -> Boolean,
): TargetedFlingBehavior =
    object : TargetedFlingBehavior {
        override suspend fun ScrollScope.performFling(
            initialVelocity: Float,
            onRemainingDistanceUpdated: (Float) -> Unit,
        ): Float {
            val start = state.requireOffset()
            val target = swipeTarget(start, threshold, state.anchors)
            if (target != SwipeAnchor.Settled && commit(target)) return 0f
            val end = state.anchors.positionOf(SwipeAnchor.Settled)
            var previous = start
            onRemainingDistanceUpdated(end - start)
            animate(start, end, animationSpec = Motion.tween(MotionSpeed.Relaxed, scale = motionScale)) { value, _ ->
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
