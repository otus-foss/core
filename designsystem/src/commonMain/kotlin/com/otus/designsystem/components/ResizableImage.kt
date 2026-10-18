package com.otus.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SignatureTokens
import com.otus.designsystem.theme.panelTextStyle
import kotlin.math.abs
import kotlin.math.roundToInt

/** A named image width the size list offers, such as Medium at 132 px. */
public class ImagePreset(
    public val name: String,
    public val width: Dp,
)

/**
 * An image's [width], the [presets] its size list offers, the [range] its handle drags within, a
 * signature's by default, and [onWidthChange] for a new width.
 */
public class ImageSizing(
    public val width: Dp,
    public val presets: List<ImagePreset>,
    public val range: ClosedRange<Dp> = SignatureTokens.minImageWidth..SignatureTokens.maxImageWidth,
    public val onWidthChange: (Dp) -> Unit,
)

/**
 * An image being written: [painter] draws it, [description] names it aloud, a [status] such as
 * "Uploading…" shows over its corner, and [actions] open from the ⋯ pill of a selected image.
 */
public class EditableImage(
    public val painter: Painter,
    public val description: String,
    public val status: String? = null,
    public val actions: List<MenuAction> = emptyList(),
)

/**
 * An [image] being written, such as in a signature or a message, at the sizing's width, keeping
 * its aspect ratio. Tapping it runs [onSelect]. A [selected] image gets a `text2` outline, a ✕
 * that runs [onRemove], and one corner handle that resizes it within the sizing's range, snapping
 * to a preset within [SignatureTokens.snapDistance]. Under it, a size pill names the preset or the
 * size in pixels and opens the presets, and a ⋯ pill opens the image's actions.
 */
@Composable
public fun ResizableImage(
    image: EditableImage,
    sizing: ImageSizing,
    selected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = image.painter
    val intrinsic = painter.intrinsicSize
    val height = sizing.width * (intrinsic.height / intrinsic.width)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PanelTokens.chipGap)) {
        Box {
            Image(
                painter,
                contentDescription = image.description,
                modifier =
                    Modifier
                        .size(sizing.width, height)
                        .clickable(onClickLabel = "Select", onClick = onSelect)
                        .then(
                            if (selected) {
                                Modifier.border(SignatureTokens.selectionOutline, OtusColors.text2)
                            } else {
                                Modifier
                            },
                        ),
                contentScale = ContentScale.Fit,
            )
            image.status?.let { ImageStatus(it, Modifier.align(Alignment.TopStart)) }
            if (selected) {
                RemoveButton(onRemove, Modifier.align(Alignment.TopEnd))
                ResizeHandle(sizing, Modifier.align(Alignment.BottomEnd))
            }
        }
        if (selected) {
            Row(horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap)) {
                ImageSizePill(sizing, height)
                if (image.actions.isNotEmpty()) MorePill(image.actions)
            }
        }
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

/** A round ⋯ pill beside the size pill that opens [actions]. */
@Composable
private fun MorePill(actions: List<MenuAction>) {
    var open by remember { mutableStateOf(false) }
    Box(
        Modifier
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = "Image options" }
            .clickable { open = true },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(PanelTokens.chipHeight).border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Symbol(Res.drawable.symbol_more_horiz) }
        if (open) ContextMenu(actions, Offset.Zero) { open = false }
    }
}

@Composable
private fun RemoveButton(
    onRemove: () -> Unit,
    modifier: Modifier,
) {
    val half = PanelTokens.touchTarget / 2
    // Centered on the corner, inside a full touch area.
    Box(
        modifier
            .offset(x = half, y = -half)
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = "Remove image" }
            .clickable(onClick = onRemove),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SignatureTokens.removeSize)
                .background(OtusColors.bgLight, CircleShape)
                .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Symbol(Res.drawable.symbol_close) }
    }
}

/** A dot on the corner inside a 48 dp touch area; dragging it changes the width. */
@Composable
private fun ResizeHandle(
    sizing: ImageSizing,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val current by rememberUpdatedState(sizing)
    var dragged by remember { mutableStateOf<Dp?>(null) }
    val half = PanelTokens.touchTarget / 2
    Box(
        modifier
            .offset(x = half, y = half)
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = "Resize image" }
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = { dragged = null }, onDragCancel = { dragged = null }) { change, drag ->
                    change.consume()
                    val start = dragged ?: current.width
                    val raw =
                        (
                            start +
                                with(density) {
                                    drag.x.toDp()
                                }
                        ).coerceIn(current.range)
                    dragged = raw
                    current.onWidthChange(snapped(raw, current.presets))
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

/** The pill naming the size, and the presets it opens. */
@Composable
private fun ImageSizePill(
    sizing: ImageSizing,
    height: Dp,
) {
    var open by remember { mutableStateOf(false) }
    val preset = sizing.presets.firstOrNull { it.width == sizing.width }
    val label = preset?.name ?: "${sizing.width.value.roundToInt()} × ${height.value.roundToInt()} px"
    Box {
        // A 36 dp pill inside a full-height touch area.
        Box(
            Modifier.heightIn(min = PanelTokens.touchTarget).clickable(onClickLabel = "Change size") { open = true },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                Modifier
                    .heightIn(min = PanelTokens.chipHeight)
                    .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
                    .padding(horizontal = PanelTokens.chipPadding * 2),
                horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap * 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = panelTextStyle())
                Symbol(Res.drawable.symbol_expand_more)
            }
        }
        if (open) {
            Dropdown({ open = false }, width = SignatureTokens.sizeListWidth) {
                sizing.presets.forEach {
                    DropdownRow(
                        it.name,
                        selected = it == preset,
                        onClick = {
                            open = false
                            sizing.onWidthChange(it.width)
                        },
                        detail = "${it.width.value.roundToInt()} px",
                    )
                }
            }
        }
    }
}

/** [width], or the preset it is within [SignatureTokens.snapDistance] of. */
internal fun snapped(
    width: Dp,
    presets: List<ImagePreset>,
): Dp = presets.firstOrNull { abs((it.width - width).value) <= SignatureTokens.snapDistance.value }?.width ?: width
