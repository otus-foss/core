package com.otusfoss.core.designsystem.shared.reorder.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.ReorderTreeTokens

/**
 * A tree of rows, flattened in display order with each row's [depth], that the user rearranges by
 * dragging a row's [ReorderableScope.DragHandle], or the row itself after a long press when it
 * uses [ReorderableScope.dragOnLongPress]. The other rows stay in place while the dragged row and
 * its nested rows follow the finger as one `bgLight` block, rounded only at its ends like a run of
 * selected inbox rows. An `accentLight` line shows where it would land: in the gap between two
 * rows, indented to the row below, or under a row, one level deeper, to drop it inside that row.
 * [canDrop] hides the line for a target the caller would refuse, and a haptic marks each new
 * target. [onDrop] gets the row's index and the target once it is released over one; the caller
 * owns the tree. [key] must return a unique, stable key for each row. Every row is composed, so
 * keep the tree short. Changing the rows cancels an active drag.
 */
@Composable
public fun <T> ReorderableTree(
    items: List<T>,
    key: (T) -> Any,
    depth: (T) -> Int,
    onDrop: (from: Int, target: TreeDropTarget) -> Unit,
    modifier: Modifier = Modifier,
    canDrop: (from: Int, target: TreeDropTarget) -> Boolean = { _, _ -> true },
    row: @Composable ReorderableScope.(T) -> Unit,
) {
    val keys = items.map(key)
    val depths = items.map(depth)
    val drag = remember { ReorderDrag(overhang = HALF) }
    SideEffect {
        drag.layout(keys, spacing = 0f) { from, offset ->
            val target = treeDropTarget(drag.rowHeights, from, offset, nestedSpan(depths, from)) ?: return@layout
            if (canDrop(from, target)) onDrop(from, target)
        }
    }
    val heights = keys.map { drag.heights[it] ?: 0 }
    val from = keys.indexOf(drag.key)
    val span = if (from >= 0) nestedSpan(depths, from) else 0
    val target = if (from >= 0) treeDropTarget(heights, from, drag.offset, span) else null
    val shown = remember(items, from, target) { target?.takeIf { canDrop(from, it) } }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(shown) { if (shown != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    val moves = rememberTreeMoves(items, canDrop, onDrop)
    val lineDepth = shown?.let { dropLineDepth(depths, it) } ?: 0
    Column(modifier.dropLine(shown?.let { dropLineY(heights, it) }, lineDepth)) {
        items.forEachIndexed { index, item ->
            val itemKey = keys[index]
            key(itemKey) {
                val scope = ReorderRow(drag, itemKey, haptics, moves[index].up, moves[index].down)
                Box(
                    Modifier.reorderSlot(
                        drag,
                        itemKey,
                        dragged = index in from..from + span,
                        OtusColors.bgRegular,
                        draggedAlpha = ReorderTreeTokens.DRAGGED_ALPHA,
                        draggedShape = blockRowShape(joinedAbove = index > from, joinedBelow = index < from + span),
                    ),
                ) { scope.row(item) }
            }
        }
    }
}

/**
 * The Move up and Move down actions of each row: into the gap above the row before it, or below
 * the row after it, when [canDrop] allows it.
 */
@Composable
private fun <T> rememberTreeMoves(
    items: List<T>,
    canDrop: (from: Int, target: TreeDropTarget) -> Boolean,
    onDrop: (from: Int, target: TreeDropTarget) -> Unit,
): List<TreeMoves> {
    val latestDrop by rememberUpdatedState(onDrop)
    return remember(items) {
        fun move(
            index: Int,
            gap: Int,
        ): (() -> Unit)? =
            TreeDropTarget
                .Before(gap)
                .takeIf { gap in 0..items.size && canDrop(index, it) }
                ?.let { target -> { latestDrop(index, target) } }
        items.indices.map { TreeMoves(up = move(it, it - 1), down = move(it, it + 2)) }
    }
}

/** One row of the dragged block: its corners are square where it meets the block's next row. */
private fun blockRowShape(
    joinedAbove: Boolean,
    joinedBelow: Boolean,
): Shape {
    val top = if (joinedAbove) ZeroCornerSize else HalfHeightCorner(SheetTokens.rowRadius)
    val bottom = if (joinedBelow) ZeroCornerSize else HalfHeightCorner(SheetTokens.rowRadius)
    return SmoothCornerShape(top, top, bottom, bottom)
}

/**
 * A [radius] corner that never rounds more than half the row's height. A shape rounded at only one
 * edge is not scaled down, so a short row's open end would otherwise curve past its middle.
 */
private data class HalfHeightCorner(
    val radius: Dp,
) : CornerSize {
    override fun toPx(
        shapeSize: Size,
        density: Density,
    ): Float = minOf(with(density) { radius.toPx() }, shapeSize.height / 2)
}

private data class TreeMoves(
    val up: (() -> Unit)?,
    val down: (() -> Unit)?,
)
