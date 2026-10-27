package com.otus.designsystem.shared.filters.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.motionTween
import kotlin.math.roundToInt

/**
 * A panel that grows down out of a control panel [collapsedHeight] tall, on the same surface,
 * border, and radius, over a scrim. Place it where the control panel sits; [modifier] sets that
 * place and the room it may fill, which is its largest height. The [header] stays put. The [body]
 * scrolls under the [footer], which floats at the bottom, and fades out over
 * [PanelTokens.fadeWidth] just above it. A plain ✕ at the top end calls [onDismiss]. While [open]
 * it handles system back; back, a tap on the scrim, and an upward swipe call [onDismiss] too.
 * Height and contents animate together, and the contents fade in while they slide
 * [FilterTokens.contentSlide] down. A [FilterPanelHeader] names the panel to accessibility
 * services. The panel takes all of its room even when the body is short, so pages of different
 * lengths keep one height and leave space above the footer.
 */
@Composable
public fun FilterPanel(
    open: Boolean,
    collapsedHeight: Dp,
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
) {
    val progress by animateFloatAsState(
        if (open) 1f else 0f,
        motionTween(MotionSpeed.Normal, if (open) MotionCurve.Enter else MotionCurve.Exit),
        label = "Filter panel",
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
        PanelSurface(progress, collapsedHeight, onDismiss, modifier) {
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    header()
                    FloatingFooter(footer, Modifier.weight(1f), body)
                }
                // Over the header's end, or over the body's when there is no header row.
                IconButton("Close filters", onDismiss, Modifier.align(Alignment.TopEnd), look = IconButtonLook.Plain) {
                    Symbol(Res.drawable.symbol_close, size = FilterTokens.closeIconSize)
                }
            }
        }
    }
}

/** The [body], which scrolls under the [footer] floating at the bottom and fades out above it. */
@Composable
private fun FloatingFooter(
    footer: @Composable () -> Unit,
    modifier: Modifier,
    body: @Composable () -> Unit,
) {
    var footerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(modifier) {
        Column(Modifier.fillMaxSize().fadeBottom(footerHeight).verticalScroll(rememberScrollState())) {
            body()
            Spacer(Modifier.height(footerHeight))
        }
        Box(
            Modifier.align(Alignment.BottomCenter).onSizeChanged {
                footerHeight = with(density) { it.height.toDp() }
            },
        ) { footer() }
    }
}

@Composable
private fun PanelSurface(
    progress: Float,
    collapsedHeight: Dp,
    onDismiss: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var lift by remember { mutableFloatStateOf(0f) }
    val closeDistance = with(LocalDensity.current) { FilterTokens.closeSwipe.toPx() }
    val shape = RoundedCornerShape(PanelTokens.panelRadius)
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = lift }
                .semantics { traversalIndex = -1f }
                .draggable(
                    rememberDraggableState { lift = (lift + it).coerceAtMost(0f) },
                    Orientation.Vertical,
                    onDragStopped = {
                        if (lift < -closeDistance) onDismiss()
                        lift = 0f
                    },
                ).clip(shape)
                .background(OtusColors.bgDark)
                .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
                // Clipping, fill, and border come first, so they take the growing height.
                .grownTo(progress, collapsedHeight)
                // Taps on the panel's own space must not reach the scrim below.
                .pointerInput(Unit) { detectTapGestures { } },
        ) {
            Column(
                Modifier.graphicsLayer {
                    alpha = progress
                    translationY = (progress - 1f) * FilterTokens.contentSlide.toPx()
                },
                content = content,
            )
        }
    }
}

/** Lays the panel out as tall as its contents at [progress] 1, and [collapsedHeight] at 0. */
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

private fun lerp(
    start: Float,
    stop: Float,
    fraction: Float,
): Float = start + (stop - start) * fraction

/**
 * Hides the content behind the last [height], where the footer floats, and fades it out over
 * [PanelTokens.fadeWidth] above that, as the pill rail fades its pills before its edge.
 */
private fun Modifier.fadeBottom(height: Dp): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val end = size.height - height.toPx()
            val start = end - PanelTokens.fadeWidth.toPx()
            // The gradient clamps to transparent below its end, so the footer's band stays clear.
            drawRect(
                Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = start, endY = end),
                topLeft = Offset(0f, start),
                blendMode = BlendMode.DstIn,
            )
        }
