package com.otus.designsystem.shared.reorder.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween

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
    ReorderableLayout(items, key, onMove, modifier, spacing, background, row)
}

@Composable
private fun <T> ReorderableLayout(
    items: List<T>,
    key: (T) -> Any,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier,
    spacing: Dp,
    background: Color,
    row: @Composable ReorderableScope.(T) -> Unit,
) {
    val keys = items.map(key)
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }
    val drag = remember { ReorderDrag() }
    SideEffect {
        drag.layout(keys, spacingPx) { from, offset ->
            val to = reorderTarget(drag.rowHeights, from, offset, spacingPx)
            if (to != from) onMove(from, to)
        }
    }
    val heights = keys.map { drag.heights[it] ?: 0 }
    val from = keys.indexOf(drag.key)
    val drop = if (from >= 0) reorderTarget(heights, from, drag.offset, spacingPx) else null
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
                    if (drop == null || dragged) 0f else reorderShift(index, from, drop, heights[from], spacingPx),
                    if (drop == null) snap() else shiftMotion,
                    label = "Reorder shift",
                )
                val scope =
                    ReorderRow(
                        drag,
                        itemKey,
                        haptics,
                        up = { onMove(index, index - 1) }.takeIf { index > 0 },
                        down = { onMove(index, index + 1) }.takeIf { index < items.lastIndex },
                    )
                Box(Modifier.reorderSlot(drag, itemKey, dragged, background, shift)) { scope.row(item) }
            }
        }
    }
}
