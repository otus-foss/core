package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.core.animate
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.lerp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The control panel unfolded into a surface for a task, such as filters or the account switcher.
 * It grows down from the panel's [collapsedHeight] on the same full-bleed surface over a scrim,
 * with a grab handle at its bottom. Place it where the control panel sits; [modifier] sets the
 * room it may fill. While [open] it handles system back; back, a tap on the scrim, and an upward
 * swipe call [onDismiss]. The [header], usually a [PanelSheetHeader] in the panel's own header
 * row, stays in place and fades in over the panel's; the [content] below it fades in while it
 * slides down.
 */
@Composable
public fun PanelSheet(
    open: Boolean,
    collapsedHeight: Dp,
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
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
        SheetSurface(progress, collapsedHeight, onDismiss, modifier) {
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
    onDismiss: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var lift by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val closeDistance = with(density) { PanelTokens.sheetCloseSwipe.toPx() }
    val closeFling = with(density) { PanelTokens.sheetCloseFling.toPx() }
    val scope = rememberCoroutineScope()
    val motionScale = LocalMotionScale.current
    val settle: (Float) -> Unit = { velocity ->
        if (lift < -closeDistance || (lift < 0f && velocity < -closeFling)) {
            onDismiss()
            lift = 0f
        } else {
            scope.launch {
                animate(lift, 0f, animationSpec = Motion.tween(scale = motionScale)) { value, _ ->
                    lift =
                        value
                }
            }
        }
    }
    // Content that scrolls hands the swipe to the sheet once it reaches its end, and takes it back
    // as the sheet returns, so a long list still closes with a swipe or a flick up.
    val connection =
        remember(settle) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (lift >= 0f || available.y <= 0f) return Offset.Zero
                    val step = available.y.coerceAtMost(-lift)
                    lift += step
                    return Offset(0f, step)
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (source != NestedScrollSource.UserInput || available.y >= 0f) return Offset.Zero
                    lift += available.y
                    return Offset(0f, available.y)
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (lift >= 0f) return Velocity.Zero
                    settle(available.y)
                    return available
                }
            }
        }
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = lift }
                .semantics { traversalIndex = -1f }
                .nestedScroll(connection)
                .draggable(
                    rememberDraggableState { lift = (lift + it).coerceAtMost(0f) },
                    Orientation.Vertical,
                    onDragStopped = { velocity -> settle(velocity) },
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
