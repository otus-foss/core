package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.otus.designsystem.elements.RichBlock
import com.otus.designsystem.elements.RichText
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The borderless editor for [text], below the header lines of a message. It grows with the text
 * and takes taps across the area the caller gives it. [placeholder] shows in `text3` while it is
 * empty and names it to accessibility services. Links draw in the accent. Each image shows what
 * [images] gives for its `src` between the text around it, a box until it gives one. A tap shows
 * its size and remove controls; Backspace at the start of the text after an image removes it.
 */
@Composable
public fun RichTextBody(
    text: RichText,
    placeholder: String,
    modifier: Modifier = Modifier,
    images: @Composable (source: String) -> EditableImage? = { null },
) {
    val linkColor = OtusColors.accentLight
    val blocks = text.blocks.toList()
    SideEffect { blocks.filterIsInstance<RichBlock.Text>().forEach { it.state.config.linkColor = linkColor } }
    val only = blocks.singleOrNull() as? RichBlock.Text
    if (only != null) {
        TextBlock(text, only, 0, placeholder, modifier)
        return
    }
    Column(modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            key(block) {
                when (block) {
                    is RichBlock.Text -> TextBlock(text, block, index, placeholder, Modifier)
                    is RichBlock.Image -> ImageBlock(text, block, index, images(block.source))
                }
            }
        }
    }
}

/** One run of text; the first names the editor and shows [placeholder] while all the text is empty. */
@Composable
private fun TextBlock(
    text: RichText,
    block: RichBlock.Text,
    index: Int,
    placeholder: String,
    modifier: Modifier,
) {
    val style = panelTextStyle()
    val focus = remember { FocusRequester() }
    LaunchedEffect(text.focusNext) {
        if (text.focusNext === block) {
            focus.requestFocus()
            text.focusNext = null
        }
    }
    BasicRichTextEditor(
        state = block.state,
        modifier =
            modifier
                .fillMaxWidth()
                .focusRequester(focus)
                .onFocusChanged {
                    if (it.isFocused) {
                        text.active = block
                        text.selected = null
                    }
                }.onPreviewKeyEvent { event ->
                    val backspace = event.key == Key.Backspace && event.type == KeyEventType.KeyDown
                    val atStart = block.state.selection.collapsed && block.state.selection.start == 0
                    (backspace && atStart && index > 0).also { if (it) text.removeImageBefore(index) }
                }.semantics { contentDescription = if (index == 0) placeholder else "$placeholder, continued" },
        textStyle = style,
        cursorBrush = SolidColor(OtusColors.accentLight),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { field ->
            Box(Modifier.padding(horizontal = PageTokens.textInset, vertical = PageTokens.contentInset)) {
                if (index == 0 && text.text.isEmpty() && text.imageSources.isEmpty()) {
                    Text(placeholder, style = style, color = OtusColors.text3)
                }
                field()
            }
        },
    )
}

/**
 * An image as wide as the text until it is resized; a `bgDark` box until it loads. Tapping it
 * shows the [ResizableImage] controls, and ✕ takes it out of the text.
 */
@Composable
private fun ImageBlock(
    text: RichText,
    block: RichBlock.Image,
    index: Int,
    image: EditableImage?,
) {
    val focus = LocalFocusManager.current
    BoxWithConstraints(Modifier.padding(horizontal = PageTokens.textInset).fillMaxWidth()) {
        val full = maxWidth
        val drawn = image?.takeIf { it.painter.intrinsicSize.isSpecified && it.painter.intrinsicSize.width > 0f }
        if (drawn == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(ComposeTokens.inlineImageLoadingHeight)
                    .clip(RoundedCornerShape(ComposeTokens.recipientRadius))
                    .background(OtusColors.bgDark),
            )
        } else {
            ResizableImage(
                drawn,
                ImageSizing(
                    block.width?.coerceAtMost(full) ?: full,
                    listOf(
                        ImagePreset("Small", ComposeTokens.inlineImageSmall),
                        ImagePreset("Medium", ComposeTokens.inlineImageMedium),
                        ImagePreset("Full width", full),
                    ),
                    onWidthChange = { block.width = it.takeIf { width -> width < full } },
                    range = ComposeTokens.inlineImageMinWidth..full,
                ),
                selected = text.selected === block,
                onSelect = {
                    focus.clearFocus()
                    text.selected = block
                },
                onRemove = { text.removeImageAt(index) },
            )
        }
    }
}
