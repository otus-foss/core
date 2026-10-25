package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.CountPill
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors

/**
 * A two-line [EntryCard]: [CompactRowText.first] and the date, then [CompactRowText.second] with
 * its marks and folders, and an optional preview. Accessibility services read
 * [CompactRowText.description] instead of the visible text, because the marks and pills are icons.
 */
@Composable
public fun CompactContentRow(
    text: CompactRowText,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selection: RowSelection? = null,
    onLongClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
) {
    EntryCard(
        onClick,
        leading,
        modifier.semantics(mergeDescendants = true) {
            contentDescription = text.description
            stateDescription = text.stateLabel
        },
        selection = selection,
        dimmed = dimmed,
        onLongClick = onLongClick,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(InboxTokens.lineGap)) {
            FirstLine(text)
            SecondLine(text)
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
private fun FirstLine(text: CompactRowText) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowLineText(text.first, firstLineStyle(), Modifier.weight(1f, fill = false))
            text.countIn(CountPlacement.AfterFirst)?.let { CountPill(it) }
        }
        text.countIn(CountPlacement.BeforeTrailing)?.let { CountPill(it) }
        Text(
            text.trailing,
            color = OtusColors.text2,
            fontSize = InboxTokens.compactDateSize,
            lineHeight = InboxTokens.compactLineHeight,
            maxLines = 1,
        )
    }
}

/** The second line with the count pill after it, then the marks, folder glyphs, and "+N". */
@Composable
private fun SecondLine(text: CompactRowText) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowLineText(text.second, secondLineStyle(), Modifier.weight(1f, fill = false))
            text.countIn(CountPlacement.AfterSecond)?.let { CountPill(it) }
        }
        RowEndIcons(text)
    }
}

private fun CompactRowText.countIn(placement: CountPlacement): String? = count?.takeIf { countPlacement == placement }

private fun firstLineStyle(): LineStyle =
    LineStyle(OtusColors.text1, InboxTokens.compactTitleSize, InboxTokens.compactTitleLineHeight, FontWeight.SemiBold)

private fun secondLineStyle(): LineStyle =
    LineStyle(OtusColors.text2, InboxTokens.compactLineSize, InboxTokens.compactLineHeight)
