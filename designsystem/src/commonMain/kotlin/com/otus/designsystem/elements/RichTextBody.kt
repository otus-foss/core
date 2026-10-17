package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.panelTextStyle

/** A formatting the [FormattingBar][com.otus.designsystem.components.FormattingBar] turns on or off. */
public enum class TextFormat { Bold, Italic, Bullets, Numbers, Link, Clear }

/**
 * Formatted text being written, such as a message body or a signature. It reads and writes
 * [html], and [text] is the same content without formatting, for a plain-text part. Reading either
 * inside a snapshot observer, such as `snapshotFlow`, follows every edit.
 */
@Stable
public class RichText(
    html: String = "",
) {
    internal val state: RichTextState = RichTextState().apply { if (html.isNotEmpty()) setHtml(html) }

    public val html: String get() = if (text.isEmpty()) "" else state.toHtml()

    public val text: String get() = state.annotatedString.text

    public fun setHtml(html: String) {
        state.setHtml(html)
    }

    /** Whether [format] applies at the cursor, such as bold text or a bulleted line. */
    public fun isActive(format: TextFormat): Boolean =
        when (format) {
            TextFormat.Bold -> state.currentSpanStyle.fontWeight == FontWeight.Bold
            TextFormat.Italic -> state.currentSpanStyle.fontStyle == FontStyle.Italic
            TextFormat.Bullets -> state.isUnorderedList
            TextFormat.Numbers -> state.isOrderedList
            TextFormat.Link -> state.isLink
            TextFormat.Clear -> false
        }

    /** Turns [format] on or off at the cursor. A link needs [addLink]; here it only removes one. */
    public fun toggle(format: TextFormat) {
        when (format) {
            TextFormat.Bold -> {
                state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
            }

            TextFormat.Italic -> {
                state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
            }

            TextFormat.Bullets -> {
                state.toggleUnorderedList()
            }

            TextFormat.Numbers -> {
                state.toggleOrderedList()
            }

            TextFormat.Link -> {
                if (state.isLink) state.removeLink()
            }

            TextFormat.Clear -> {
                state.clearSpanStyles()
                state.clearRichSpans()
                if (state.isUnorderedList) state.removeUnorderedList()
                if (state.isOrderedList) state.removeOrderedList()
            }
        }
    }

    /** Links the selected text to [url], or inserts [url] as its own text when nothing is selected. */
    public fun addLink(url: String) {
        if (state.selection.collapsed) state.addLink(url, url) else state.addLinkToSelection(url)
    }
}

/**
 * The borderless editor for [text], below the header lines of a message. It grows with the text
 * and takes taps across the area the caller gives it. [placeholder] shows in `text3` while it is
 * empty and names it to accessibility services. Links draw in the accent.
 */
@Composable
public fun RichTextBody(
    text: RichText,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val style = panelTextStyle()
    val linkColor = OtusColors.accentLight
    SideEffect { text.state.config.linkColor = linkColor }
    BasicRichTextEditor(
        state = text.state,
        modifier = modifier.fillMaxWidth().semantics { contentDescription = placeholder },
        textStyle = style,
        cursorBrush = SolidColor(OtusColors.accentLight),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { field ->
            Box(Modifier.padding(horizontal = PageTokens.textInset, vertical = PageTokens.contentInset)) {
                if (text.text.isEmpty()) Text(placeholder, style = style, color = OtusColors.text3)
                field()
            }
        },
    )
}
