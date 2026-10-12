package com.otus.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_drag_indicator
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween

/**
 * A column whose rows the user reorders by dragging each row's [ReorderableScope.DragHandle]. The
 * row follows the finger within the column while the others step aside, and a haptic marks each
 * new place. [onMove] reports the row's old and new index once it is dropped; the caller owns the
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
    val keys = items.map(key)
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }
    val drag = remember { ReorderDrag() }
    SideEffect { drag.layout(keys, spacingPx, onMove) }
    val heights = keys.map { drag.heights[it] ?: 0 }
    val from = keys.indexOf(drag.key)
    val target = if (from >= 0) reorderTarget(heights, from, drag.offset, spacingPx) else null
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(target) { if (target != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.forEachIndexed { index, item ->
            val itemKey = keys[index]
            key(itemKey) {
                val dragged = index == from
                // Rows snap back into their new places on drop instead of animating from their old ones.
                val shiftMotion = motionTween<Float>(MotionSpeed.Fast)
                val shift by animateFloatAsState(
                    if (target == null || dragged) 0f else reorderShift(index, from, target, heights[from], spacingPx),
                    if (target == null) snap() else shiftMotion,
                    label = "Reorder shift",
                )
                val scope =
                    ReorderRow(
                        drag,
                        itemKey,
                        up = { onMove(index, index - 1) }.takeIf { index > 0 },
                        down = { onMove(index, index + 1) }.takeIf { index < items.lastIndex },
                    )
                Box(
                    Modifier
                        .zIndex(if (dragged) 1f else 0f)
                        .graphicsLayer { translationY = if (dragged) drag.offset else shift }
                        .background(background)
                        .onSizeChanged { drag.heights[itemKey] = it.height },
                ) { scope.row(item) }
            }
        }
    }
}

private class ReorderRow(
    private val drag: ReorderDrag,
    private val key: Any,
    private val up: (() -> Unit)?,
    private val down: (() -> Unit)?,
) : ReorderableScope {
    @Composable
    override fun DragHandle(label: String) {
        Box(
            Modifier
                .size(PanelTokens.touchTarget, PanelTokens.compactRowHeight)
                .semantics {
                    contentDescription = "Reorder $label"
                    customActions = listOfNotNull(moveAction("Move up", up), moveAction("Move down", down))
                }.pointerInput(drag, key) {
                    detectVerticalDragGestures(
                        onDragStart = { drag.start(key) },
                        onDragEnd = drag::drop,
                        onDragCancel = drag::cancel,
                    ) { change, amount ->
                        change.consume()
                        drag.moveBy(amount)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_drag_indicator)
            }
        }
    }
}

private fun moveAction(
    label: String,
    move: (() -> Unit)?,
): CustomAccessibilityAction? =
    move?.let { run ->
        CustomAccessibilityAction(label) {
            run()
            true
        }
    }
