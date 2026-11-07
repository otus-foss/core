package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.otus.designsystem.theme.colors.OtusColors

/**
 * What a [Chip] says: its [text], an optional `text3` [prefix] before it, such as the parent
 * folders of a folder whose name repeats, a [detail] after a short divider line, such as the
 * mailboxes beside an account, and what screen readers hear, by default prefix and text, such as
 * a folder's full path. A null [text] leaves only the chip's leading content and the detail.
 */
public data class ChipLabel(
    val text: String?,
    val prefix: String? = null,
    val detail: String? = null,
    val spoken: String = listOfNotNull(prefix, text).joinToString(", "),
)

/** How a [Chip] reads to accessibility services, and how it toggles. */
public enum class ChipRole {
    /** One of several choices that can be on together. */
    Checkbox,

    /** One choice of a group where only one is on. */
    Radio,

    /** Opens something, such as a list of every folder; the chip's `selected` is ignored. */
    Button,
}

/** The label's text after its `text3` prefix and a slash, such as "Freelancing / Invoices". */
@Composable
internal fun ChipLabel.annotated(): AnnotatedString {
    val faint = OtusColors.text3
    val label = this
    return buildAnnotatedString {
        val prefix = label.prefix
        if (prefix != null) withStyle(SpanStyle(color = faint)) { append("$prefix / ") }
        append(label.text.orEmpty())
    }
}
