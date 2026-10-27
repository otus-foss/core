package com.otus.designsystem.mail.inbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_attach_file
import com.otus.designsystem.resources.symbol_bookmark_fill
import com.otus.designsystem.resources.symbol_event
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.resources.symbol_reply
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.folders.elements.FolderGlyph
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors
import org.jetbrains.compose.resources.DrawableResource

/** [line]'s icon in text2, then its parts on one line, with quiet parts in text3. */
@Composable
internal fun RowLineText(
    line: RowLine,
    style: LineStyle,
    modifier: Modifier = Modifier,
) {
    val quiet = OtusColors.text3
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        line.icon?.let { MarkSymbol(it, OtusColors.text2) }
        Text(
            buildAnnotatedString {
                line.parts.forEach { part ->
                    if (part.quiet) withStyle(SpanStyle(color = quiet)) { append(part.text) } else append(part.text)
                }
            },
            Modifier.weight(1f, fill = false),
            color = style.color,
            fontSize = style.size,
            lineHeight = style.lineHeight,
            fontWeight = style.weight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The marks, folder glyphs, and "+N" at the end of line 2, each [InboxTokens.markSize] wide. */
@Composable
internal fun RowEndIcons(text: CompactRowText) {
    text.marks.forEach { mark ->
        when (mark) {
            RowMark.Answered -> MarkSymbol(Res.drawable.symbol_reply, OtusColors.text2)
            RowMark.Attachment -> MarkSymbol(Res.drawable.symbol_attach_file, OtusColors.text2)
            RowMark.Invite -> MarkSymbol(Res.drawable.symbol_event, OtusColors.text2)
            RowMark.Flagged -> MarkSymbol(Res.drawable.symbol_flag, OtusColors.text1)
            RowMark.ReadLater -> MarkSymbol(Res.drawable.symbol_bookmark_fill, OtusColors.text1)
        }
    }
    text.folders.forEach { FolderGlyph(it.color, kind = it.kind) }
    if (text.moreFolders > 0) {
        Text(
            "+${text.moreFolders}",
            color = OtusColors.text3,
            fontSize = InboxTokens.moreFoldersSize,
            lineHeight = InboxTokens.moreFoldersSize,
            maxLines = 1,
        )
    }
}

@Composable
private fun MarkSymbol(
    resource: DrawableResource,
    color: Color,
) {
    CompositionLocalProvider(LocalContentColor provides color) { Symbol(resource, size = InboxTokens.markSize) }
}
