package com.otus.designsystem.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.EntryTokens
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SwipeTokens
import kotlin.math.abs

/**
 * The accent card behind a swiped row, the row card's full size and shape, which the row slides
 * off. The action's icon and label sit at the edge the row uncovers. The icon grows and slides in
 * until the drag [distance] reaches the threshold. A release that commits brightens the card by
 * [flash]. [onMeasured] reports the width the label needs.
 */
@Composable
internal fun SwipeBackdrop(
    action: SwipeAction,
    distance: Float,
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
    Box(
        modifier
            .clip(RoundedCornerShape(EntryTokens.radius))
            .background(OtusColors.accentRegular)
            .drawBehind { if (flash > 0f) drawRect(OtusColors.text1.copy(alpha = flash)) },
    ) {
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
            animate(
                start,
                end,
                animationSpec = Motion.tween(MotionSpeed.Relaxed, scale = motionScale),
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
