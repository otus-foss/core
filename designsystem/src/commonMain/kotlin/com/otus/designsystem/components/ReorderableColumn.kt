package com.otus.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.motionTween

/**
 * A column whose rows the user reorders by dragging each row's [ReorderableScope.DragHandle], or
 * the row itself after a long press when it uses [ReorderableScope.dragOnLongPress]. The row
 * follows the finger within the column while the others step aside, and a haptic marks each new
 * place. [onMove] reports the row's old and new index once it is dropped; the caller owns the
 * order. [key] must return a unique, stable key for each row. Every row is composed, so keep the
 * list short. Changing the order or removing a row cancels an active drag. Each row is painted
 * with [background], which must match the surface behind the column so a moving row hides the
 * rows it passes over.
 */
@Composable
public fun <T> ReorderableColumn(
    items: List<T>,
    key: (T) -> Any,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    background: Color = OtusColors.bgRegular,
    row: @Composable ReorderableScope.(T) -> Unit,
) {
    ReorderableLayout(items, key, ReorderCallbacks(onMove, onNest = null), modifier, spacing, background, row)
}

/**
 * A [ReorderableColumn] whose rows also take drops onto them, such as a folder dropped onto
 * another folder to nest it. While the dragged row's leading edge is in the middle half of another
 * row, that row gets an `accentDark` fill, and dropping there calls [onNest] with the dragged
 * row's index and that row's index. Near a row's edges the drag reorders and calls [onMove]. Rows
 * follow each other without gaps.
 */
@Composable
public fun <T> ReorderableColumn(
    items: List<T>,
    key: (T) -> Any,
    onMove: (from: Int, to: Int) -> Unit,
    onNest: (from: Int, onto: Int) -> Unit,
    modifier: Modifier = Modifier,
    background: Color = OtusColors.bgRegular,
    row: @Composable ReorderableScope.(T) -> Unit,
) {
    ReorderableLayout(items, key, ReorderCallbacks(onMove, onNest), modifier, 0.dp, background, row)
}

private class ReorderCallbacks(
    val onMove: (Int, Int) -> Unit,
    val onNest: ((Int, Int) -> Unit)?,
)

@Composable
private fun <T> ReorderableLayout(
    items: List<T>,
    key: (T) -> Any,
    callbacks: ReorderCallbacks,
    modifier: Modifier,
    spacing: Dp,
    background: Color,
    row: @Composable ReorderableScope.(T) -> Unit,
) {
    val keys = items.map(key)
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }
    val drag = remember { ReorderDrag() }
    SideEffect { drag.layout(keys, spacingPx, callbacks.onMove, callbacks.onNest) }
    val heights = keys.map { drag.heights[it] ?: 0 }
    val from = keys.indexOf(drag.key)
    val drop = if (from >= 0) reorderDrop(heights, from, drag.offset, spacingPx, callbacks.onNest != null) else null
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(drop) { if (drop != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.forEachIndexed { index, item ->
            val itemKey = keys[index]
            key(itemKey) {
                val dragged = index == from
                // Rows snap back into their new places on drop instead of animating from their old ones.
                val shiftMotion = motionTween<Float>(MotionSpeed.Fast)
                val shift by animateFloatAsState(
                    if (drop == null || dragged) 0f else reorderShift(index, from, drop.to, heights[from], spacingPx),
                    if (drop == null) snap() else shiftMotion,
                    label = "Reorder shift",
                )
                val scope =
                    ReorderRow(
                        drag,
                        itemKey,
                        haptics,
                        up = { callbacks.onMove(index, index - 1) }.takeIf { index > 0 },
                        down = { callbacks.onMove(index, index + 1) }.takeIf { index < items.lastIndex },
                    )
                Box(
                    Modifier
                        .zIndex(if (dragged) 1f else 0f)
                        .graphicsLayer { translationY = if (dragged) drag.offset else shift }
                        .background(background)
                        .then(if (index == drop?.onto) Modifier.nestHighlight() else Modifier)
                        .onSizeChanged { drag.heights[itemKey] = it.height },
                ) { scope.row(item) }
            }
        }
    }
}

/** Marks the row a dragged row would drop onto. */
private fun Modifier.nestHighlight(): Modifier =
    background(OtusColors.accentDark, RoundedCornerShape(SheetTokens.rowRadius))
