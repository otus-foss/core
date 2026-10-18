package com.otus.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SignatureTokens
import com.otus.designsystem.theme.panelTextStyle
import kotlin.math.abs
import kotlin.math.roundToInt

/** A named image width, such as Medium at 132 px; [short] labels its guide while dragging. */
public class ImagePreset(
    public val name: String,
    public val width: Dp,
    public val short: String = name.take(1),
)

/**
 * An image's [width], the [presets] an [ImageBar] and the drag guides offer, the [range] its
 * handle drags within, a signature's by default, and [onWidthChange] for a new width.
 */
public class ImageSizing(
    public val width: Dp,
    public val presets: List<ImagePreset>,
    public val range: ClosedRange<Dp> = SignatureTokens.minImageWidth..SignatureTokens.maxImageWidth,
    public val onWidthChange: (Dp) -> Unit,
)

/**
 * An image being written: [painter] draws it, [description] names it aloud, and a [status] such as
 * "Uploading…" shows over its corner.
 */
public class EditableImage(
    public val painter: Painter,
    public val description: String,
    public val status: String? = null,
)

/**
 * An [image] being written, such as in a signature or a message, at the sizing's width, keeping
 * its aspect ratio. Tapping it runs [onSelect]; a [selected] image gets a `text2` outline and a
 * corner handle, and its other controls live in an [ImageBar]. Dragging the handle resizes it
 * within the sizing's range: a dashed guide marks each preset, the width clicks into one within
 * [SignatureTokens.snapDistance] with a haptic tick, and a pill under the handle names it.
 */
@Composable
public fun ResizableImage(
    image: EditableImage,
    sizing: ImageSizing,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val intrinsic = image.painter.intrinsicSize
    val height = sizing.width * (intrinsic.height / intrinsic.width)
    val dragging = remember { mutableStateOf(false) }
    // Above what follows it, which the handle and its guides reach over.
    Box(modifier.zIndex(if (selected) 1f else 0f).then(if (dragging.value) Modifier.sizeGuides(sizing) else Modifier)) {
        Image(
            image.painter,
            contentDescription = image.description,
            modifier =
                Modifier
                    .size(sizing.width, height)
                    .clickable(onClickLabel = "Select", onClick = onSelect)
                    .then(
                        if (selected) Modifier.border(SignatureTokens.selectionOutline, OtusColors.text2) else Modifier,
                    ),
            contentScale = ContentScale.Fit,
        )
        image.status?.let { ImageStatus(it, Modifier.align(Alignment.TopStart)) }
        if (selected) ResizeHandle(sizing, dragging, Modifier.align(Alignment.BottomEnd))
        if (dragging.value) SizeLabel(sizing, height, Modifier.align(Alignment.BottomEnd))
    }
}

/** A short note over the image's corner, such as "Uploading…". */
@Composable
private fun ImageStatus(
    text: String,
    modifier: Modifier,
) {
    Text(
        text,
        modifier
            .padding(PanelTokens.iconGap)
            .background(OtusColors.bgDark, CircleShape)
            .padding(horizontal = PanelTokens.chipPadding, vertical = PanelTokens.iconGap),
        style = panelTextStyle(),
        color = OtusColors.text2,
    )
}

/** A dot on the corner inside a 48 dp touch area; dragging it changes the width while [dragging] is on. */
@Composable
private fun ResizeHandle(
    sizing: ImageSizing,
    dragging: MutableState<Boolean>,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val current by rememberUpdatedState(sizing)
    var dragged by remember { mutableStateOf<Dp?>(null) }
    val half = PanelTokens.touchTarget / 2
    Box(
        modifier
            .offset(x = half, y = half)
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = "Resize image" }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragging.value = true },
                    onDragEnd = {
                        dragged = null
                        dragging.value = false
                    },
                    onDragCancel = {
                        dragged = null
                        dragging.value = false
                    },
                ) { change, drag ->
                    change.consume()
                    val raw = ((dragged ?: current.width) + with(density) { drag.x.toDp() }).coerceIn(current.range)
                    dragged = raw
                    val width = snapped(raw, current.presets)
                    if (width != current.width && current.presets.any { it.width == width }) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                    current.onWidthChange(width)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SignatureTokens.handleSize)
                .background(OtusColors.text1, CircleShape)
                .border(PanelTokens.borderWidth, OtusColors.bgDark, CircleShape),
        )
    }
}

/** The pill under the handle while dragging: the preset's name, or the size in pixels. */
@Composable
private fun SizeLabel(
    sizing: ImageSizing,
    height: Dp,
    modifier: Modifier,
) {
    val label =
        sizing.presets.firstOrNull { it.width == sizing.width }?.name
            ?: "${sizing.width.value.roundToInt()} × ${height.value.roundToInt()} px"
    Text(
        label,
        modifier
            .offset(x = PanelTokens.touchTarget / 2, y = PanelTokens.touchTarget)
            .background(OtusColors.text1, CircleShape)
            .padding(horizontal = PanelTokens.chipPadding, vertical = PanelTokens.iconGap),
        style = panelTextStyle(),
        color = OtusColors.bgDark,
    )
}

/**
 * A dashed line at each preset's width, reaching past the image, with its short label above and
 * beside it; the preset the width matches draws in `text1` and bold.
 */
@Composable
private fun Modifier.sizeGuides(sizing: ImageSizing): Modifier {
    val measurer = rememberTextMeasurer()
    val style = panelTextStyle().merge(TextStyle(fontSize = ComposeTokens.detailSize))
    return drawBehind {
        val reach = ComposeTokens.guideReach.toPx()
        val dash =
            PathEffect.dashPathEffect(
                floatArrayOf(ComposeTokens.guideDash.toPx(), ComposeTokens.guideDash.toPx()),
            )
        sizing.presets.forEach { preset ->
            val chosen = preset.width == sizing.width
            val color = if (chosen) OtusColors.text1 else OtusColors.text3
            val x = preset.width.toPx()
            drawLine(
                color,
                Offset(x, -reach),
                Offset(x, size.height + reach),
                PanelTokens.borderWidth.toPx(),
                pathEffect = dash,
            )
            val text =
                measurer.measure(
                    preset.short,
                    style.copy(color = color, fontWeight = if (chosen) FontWeight.Bold else null),
                )
            // A label that would pass the widest size sits left of its line instead.
            val gap = PanelTokens.iconGap.toPx()
            val right = x + gap + text.size.width <= sizing.range.endInclusive.toPx()
            val left = if (right) x + gap else x - gap - text.size.width
            drawText(text, topLeft = Offset(left, -reach - text.size.height))
        }
    }
}

/** [width], or the preset it is within [SignatureTokens.snapDistance] of. */
internal fun snapped(
    width: Dp,
    presets: List<ImagePreset>,
): Dp = presets.firstOrNull { abs((it.width - width).value) <= SignatureTokens.snapDistance.value }?.width ?: width
