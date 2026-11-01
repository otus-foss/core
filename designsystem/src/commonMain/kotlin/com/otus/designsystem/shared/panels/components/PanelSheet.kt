package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import kotlin.math.roundToInt

/**
 * The control panel unfolded into a surface for a task, such as filters or the account switcher.
 * It grows down from the panel's [collapsedHeight] on the same full-bleed surface over a scrim,
 * with a grab handle at its bottom. Place it where the control panel sits; [modifier] sets the
 * room it may fill. While [open] it handles system back; back, a tap on the scrim, and an upward
 * swipe call [onDismiss]; a pull down on it calls [onPullDown], when given, such as to open the
 * next step of the panel, without moving the sheet. The [header], usually a [PanelSheetHeader]
 * in the panel's own header row, stays in place and fades in over the panel's; the [content]
 * below it fades in while it slides down.
 */
@Composable
public fun PanelSheet(
    open: Boolean,
    collapsedHeight: Dp,
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onPullDown: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val progress by animateFloatAsState(
        if (open) 1f else 0f,
        motionTween(MotionSpeed.Normal, if (open) MotionCurve.Enter else MotionCurve.Exit),
        label = "Panel sheet",
    )
    if (!open && progress == 0f) return
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = open,
        onBackCompleted = onDismiss,
    )
    Box(Modifier.fillMaxSize()) {
        // Taps only: the scrim is not a control screen readers should stop on.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress }
                .background(OtusColors.scrim.copy(alpha = SheetTokens.SCRIM_ALPHA))
                .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
        )
        SheetSurface(progress, collapsedHeight, SheetPulls(onDismiss, onPullDown), modifier) {
            Box(Modifier.graphicsLayer { alpha = progress }) { header() }
            // Not filling, so the handle keeps its place below content that fills the room.
            Column(
                Modifier.weight(1f, fill = false).graphicsLayer {
                    alpha = progress
                    translationY = (progress - 1f) * PanelTokens.sheetContentSlide.toPx()
                },
                content = content,
            )
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = PanelTokens.handlePadding)
                    .size(PanelTokens.handleWidth, PanelTokens.handleHeight)
                    .clip(CircleShape)
                    .background(OtusColors.bgLight),
            )
        }
    }
}

/**
 * The first row of a [PanelSheet], in the control panel's header place below the status bar: the
 * [navigation] button, the [content], such as a title, and [actions] at the end, such as Reset.
 */
@Composable
public fun PanelSheetHeader(
    navigation: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier) { PanelHeader(navigation, HeaderAction(null, shown = false), actions, content) }
}

@Composable
private fun SheetSurface(
    progress: Float,
    collapsedHeight: Dp,
    pulls: SheetPulls,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var lift by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val closeDistance = with(density) { PanelTokens.sheetCloseSwipe.toPx() }
    val pullDistance = with(density) { QuickViewTokens.pullDistance.toPx() }
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = lift.coerceAtMost(0f) }
                .semantics { traversalIndex = -1f }
                .draggable(
                    rememberDraggableState {
                        val most = if (pulls.onPullDown == null) 0f else Float.MAX_VALUE
                        lift = (lift + it).coerceAtMost(most)
                    },
                    Orientation.Vertical,
                    onDragStopped = {
                        when {
                            lift < -closeDistance -> pulls.onDismiss()
                            lift >= pullDistance -> pulls.onPullDown?.invoke()
                        }
                        lift = 0f
                    },
                ).panelEdge()
                // The surface comes first, so it takes the growing height.
                .grownTo(progress, collapsedHeight)
                // Taps on the panel's own space must not reach the scrim below.
                .pointerInput(Unit) { detectTapGestures { } },
            content = content,
        )
    }
}

/**
 * The control panel's full-bleed surface on any shape that grows from it: square at the top and
 * sides, rounded at the bottom corners, and stroked along the bottom edge only.
 */
internal fun Modifier.panelEdge(): Modifier =
    clip(PanelTokens.bottomShape).drawBehind {
        val border = PanelTokens.borderWidth.toPx()
        val radius = CornerRadius(PanelTokens.bottomRadius.toPx())
        drawRect(OtusColors.bgDark)
        // Drawn past the top and sides, which the clip cuts away, so only the bottom stroke shows.
        clipRect {
            drawRoundRect(
                OtusColors.strokeBgDark,
                topLeft = Offset(-border, -radius.y),
                size = Size(size.width + border * 2, size.height + radius.y - border / 2),
                cornerRadius = radius,
                style = Stroke(border),
            )
        }
    }

/** Lays the surface out as tall as its contents at [progress] 1, and [collapsedHeight] at 0. */
private fun Modifier.grownTo(
    progress: Float,
    collapsedHeight: Dp,
): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0))
        val start = collapsedHeight.roundToPx().coerceAtMost(placeable.height)
        val height = lerp(start.toFloat(), placeable.height.toFloat(), progress).roundToInt()
        layout(placeable.width, height) { placeable.place(0, 0) }
    }

/** What an upward swipe and a downward pull on a [PanelSheet] do. */
private class SheetPulls(
    val onDismiss: () -> Unit,
    val onPullDown: (() -> Unit)?,
)
