package com.otus.designsystem.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The borderless editor for [text], below the header lines of a message. It grows with the text
 * and takes taps across the area the caller gives it. [placeholder] shows in `text3` while it is
 * empty and names it to accessibility services. Links draw in the accent. Each image shows what
 * [images] gives for its `src` between the text around it; Backspace at the start of the text
 * after an image removes the image.
 */
@Composable
public fun RichTextBody(
    text: RichText,
    placeholder: String,
    modifier: Modifier = Modifier,
    images: @Composable (source: String) -> Painter? = { null },
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
                    is RichBlock.Image -> ImageBlock(images(block.source))
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
                .onFocusChanged { if (it.isFocused) text.active = block }
                .onPreviewKeyEvent { event ->
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
 * An image as wide as the text and at most [ComposeTokens.inlineImageMaxHeight] tall, keeping its
 * shape; a `bgDark` box until it loads.
 */
@Composable
private fun ImageBlock(painter: Painter?) {
    val shape = RoundedCornerShape(ComposeTokens.recipientRadius)
    Box(Modifier.padding(horizontal = PageTokens.textInset).fillMaxWidth()) {
        val size = painter?.intrinsicSize?.takeIf { it.isSpecified && it.width > 0f && it.height > 0f }
        if (painter == null || size == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(
                        ComposeTokens.inlineImageMaxHeight / 2,
                    ).clip(shape)
                    .background(OtusColors.bgDark),
            )
        } else {
            Image(
                painter,
                contentDescription = "Image",
                modifier =
                    Modifier
                        .heightIn(max = ComposeTokens.inlineImageMaxHeight)
                        .aspectRatio(size.width / size.height)
                        .clip(shape),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
