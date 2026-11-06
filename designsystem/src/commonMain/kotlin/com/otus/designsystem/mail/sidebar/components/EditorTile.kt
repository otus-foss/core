package com.otus.designsystem.mail.sidebar.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import com.otus.designsystem.mail.sidebar.elements.SidebarTileSize
import com.otus.designsystem.mail.sidebar.elements.TileRemoveBadge
import com.otus.designsystem.mail.sidebar.elements.TileResizeHandle
import com.otus.designsystem.shared.reorder.components.detectLongPressDrag
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.SidebarTokens

/**
 * One tile of a [SidebarTileEditor] at [bounds], or at [lifted] while the user drags it. It slides
 * to new bounds. A long press lifts it, the bottom-end arc resizes it, and the top-start minus
 * removes it. Accessibility services get Move and Resize actions.
 */
@Composable
internal fun EditorTile(
    bounds: IntRect,
    lifted: IntOffset?,
    edits: TileEdits,
    content: @Composable () -> Unit,
) {
    val latest by rememberUpdatedState(edits)
    val latestWidth by rememberUpdatedState(bounds.width)
    val towardEnd = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val motion = motionTween<IntOffset>(MotionSpeed.Fast)
    val offset by animateIntOffsetAsState(lifted ?: bounds.topLeft, if (lifted != null) snap() else motion)
    val width by animateIntAsState(bounds.width, motionTween(MotionSpeed.Fast))
    val height by animateIntAsState(bounds.height, motionTween(MotionSpeed.Fast))
    val density = LocalDensity.current
    Box(
        Modifier
            .zIndex(if (lifted != null) 1f else 0f)
            .offset { offset }
            .size(with(density) { width.toDp() }, with(density) { height.toDp() })
            // On the whole tile, so a hold on its resize arc or minus lifts it too.
            .liftOnLongPress { latest },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = if (lifted != null) SidebarTokens.LIFTED_SCALE else 1f
                    scaleX = scale
                    scaleY = scale
                }.semantics(mergeDescendants = true) { customActions = edits.actions() },
            propagateMinConstraints = true,
        ) { content() }
        TileResizeHandle(
            edits.label,
            modifier = Modifier.align(Alignment.BottomEnd).resizeOnDrag(towardEnd, { latestWidth }) { latest },
        )
        // Centered just inside the tile's corner, like Control Center's minus.
        val pull = (PanelTokens.touchTarget - SidebarTokens.removeBadge) / 2 + SidebarTokens.inset
        TileRemoveBadge(edits.label, { latest.onRemove() }, Modifier.offset(-pull, -pull))
    }
}

/** It sees each touch before the resize arc and the minus, so once the hold is long enough the drag is its own. */
private fun Modifier.liftOnLongPress(edits: () -> TileEdits): Modifier =
    pointerInput(Unit) {
        detectLongPressDrag(
            onStart = { edits().moves.onLift() },
            onDrag = { edits().moves.onDrag(it) },
            onEnd = { edits().moves.onDrop() },
            onCancel = { edits().moves.onCancel() },
        )
    }

/**
 * Dragging sideways sets the size whose width is nearest the tile's [width] at the start of the
 * drag plus the drag; [towardEnd] is 1 or -1, so a drag toward the end edge widens the tile. The
 * gesture outlives the resizes it makes.
 */
private fun Modifier.resizeOnDrag(
    towardEnd: Float,
    width: () -> Int,
    edits: () -> TileEdits,
): Modifier =
    pointerInput(towardEnd) {
        var reach = 0f
        detectHorizontalDragGestures(onDragStart = { reach = width().toFloat() }) { change, amount ->
            change.consume()
            reach += amount * towardEnd
            val size = edits().sizeAt(reach)
            if (size != edits().size) edits().onResize(size)
        }
    }

private fun TileEdits.actions(): List<CustomAccessibilityAction> =
    listOfNotNull(
        moves.onEarlier?.let { action("Move earlier", it) },
        moves.onLater?.let { action("Move later", it) },
        SidebarTileSize.entries.getOrNull(size.ordinal + 1)?.let { action("Make larger") { onResize(it) } },
        SidebarTileSize.entries.getOrNull(size.ordinal - 1)?.let { action("Make smaller") { onResize(it) } },
    )

private fun action(
    label: String,
    run: () -> Unit,
) = CustomAccessibilityAction(label) {
    run()
    true
}
