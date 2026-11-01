package com.otus.designsystem.shared.reorder.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * The box around one row of a reorderable list, keyed by [key]. It measures the row for [drag],
 * paints it with [background], and moves it by [shift]. The [dragged] row follows the finger
 * above the others with a `bgLight` fill in [draggedShape], at [draggedAlpha] so a row under it can
 * show through.
 */
internal fun Modifier.reorderSlot(
    drag: ReorderDrag,
    key: Any,
    dragged: Boolean,
    background: Color,
    shift: Float = 0f,
    draggedAlpha: Float = 1f,
    draggedShape: Shape = SmoothCornerShape(SheetTokens.rowRadius),
): Modifier =
    zIndex(if (dragged) 1f else 0f)
        .graphicsLayer {
            translationY = if (dragged) drag.offset else shift
            alpha = if (dragged) draggedAlpha else 1f
        }.background(background)
        .draggedBackground(dragged, draggedShape)
        .onSizeChanged { drag.heights[key] = it.height }

private fun Modifier.draggedBackground(
    dragged: Boolean,
    shape: Shape,
): Modifier = if (dragged) background(OtusColors.bgLight, shape) else this
