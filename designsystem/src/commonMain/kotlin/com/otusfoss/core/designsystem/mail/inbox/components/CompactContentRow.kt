package com.otusfoss.core.designsystem.mail.inbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.mail.inbox.elements.MailboxQuickUse
import com.otusfoss.core.designsystem.shared.controls.elements.CountPill
import com.otusfoss.core.designsystem.shared.rows.components.EntryCard
import com.otusfoss.core.designsystem.shared.rows.components.EntryNeighbors
import com.otusfoss.core.designsystem.shared.rows.components.EntrySize
import com.otusfoss.core.designsystem.shared.rows.components.EntryTaps
import com.otusfoss.core.designsystem.shared.rows.components.RowSelection
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.rows.InboxTokens

/**
 * A two-line [EntryCard]: [CompactRowText.first] and the date, then [CompactRowText.second] with
 * its marks and folders, or a [MailboxQuickUse] for [CompactRowText.quickUse] in their place, and
 * an optional preview. Accessibility services read
 * [CompactRowText.description] instead of the visible text, because the marks and pills are icons.
 *
 * A [read] row steps back through weight and color, its text, icons, and count all in text3, never
 * opacity, so its avatar stays as bright as an unread one. While selected it keeps its weight but takes the unread
 * colors, so the text reads on the accent fill. Give [leading] an avatar of [size]'s
 * [EntrySize.avatarSize]. [neighbors] rounds the card less where other rows touch it.
 */
@Composable
public fun CompactContentRow(
    text: CompactRowText,
    read: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selection: RowSelection? = null,
    neighbors: EntryNeighbors = EntryNeighbors(),
    size: EntrySize = EntrySize.Compact,
    onLongClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
) {
    EntryCard(
        EntryTaps(onClick, onLongClick),
        leading,
        modifier.semantics(mergeDescendants = true) {
            contentDescription = text.description
            stateDescription = text.stateLabel
        },
        selection = selection,
        neighbors = neighbors,
        size = size,
    ) {
        val tone = RowTone(read, quiet = read && selection?.selected != true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(InboxTokens.lineGap)) {
            FirstLine(text, tone)
            SecondLine(text, tone)
            if (text.previewLines > 0 && text.preview.isNotEmpty()) {
                Text(
                    text.preview,
                    color = OtusColors.text3,
                    fontSize = InboxTokens.compactLineSize,
                    lineHeight = InboxTokens.compactLineHeight,
                    maxLines = text.previewLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** The first line, then the count pill right after it or before the date, then the date. */
@Composable
private fun FirstLine(
    text: CompactRowText,
    tone: RowTone,
) {
    CountedLine(text.first, tone.first, tone, text.countIn(CountPlacement.AfterFirst)) {
        val count = text.countIn(CountPlacement.BeforeTrailing)
        if (count != null) CountPill(count, color = tone.detail)
        Text(
            text.trailing,
            color = tone.detail,
            fontSize = InboxTokens.compactDateSize,
            lineHeight = InboxTokens.compactLineHeight,
            maxLines = 1,
        )
    }
}

/** The second line with the count pill after it, then the marks, folder glyphs, and "+N", or the quick use. */
@Composable
private fun SecondLine(
    text: CompactRowText,
    tone: RowTone,
) {
    CountedLine(text.second, tone.second, tone, text.countIn(CountPlacement.AfterSecond)) {
        val quickUse = text.quickUse
        if (quickUse != null) MailboxQuickUse(quickUse) else RowEndIcons(text, tone.detail)
    }
}

/** [line] with the [count] pill right after it, shrinking first, then [end], which never shrinks. */
@Composable
private fun CountedLine(
    line: RowLine,
    style: LineStyle,
    tone: RowTone,
    count: String?,
    end: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowLineText(line, style, tone.detail, Modifier.weight(1f, fill = false))
            if (count != null) CountPill(count, color = tone.detail)
        }
        end()
    }
}

private fun CompactRowText.countIn(placement: CountPlacement): String? = count?.takeIf { countPlacement == placement }

/**
 * How a row's text and small parts draw. A [quiet] row, one read and not selected, draws in text3
 * throughout; [detail] is the color of the date, the count pill, and the plain icons and marks.
 */
private class RowTone(
    read: Boolean,
    quiet: Boolean,
) {
    val first: LineStyle =
        LineStyle(
            if (quiet) OtusColors.text3 else OtusColors.text1,
            InboxTokens.compactTitleSize,
            InboxTokens.compactTitleLineHeight,
            if (read) FontWeight.Normal else FontWeight.SemiBold,
        )
    val second: LineStyle =
        LineStyle(
            if (quiet) OtusColors.text3 else OtusColors.text2,
            InboxTokens.compactLineSize,
            InboxTokens.compactLineHeight,
        )
    val detail: Color = if (quiet) OtusColors.text3 else OtusColors.text2
}
