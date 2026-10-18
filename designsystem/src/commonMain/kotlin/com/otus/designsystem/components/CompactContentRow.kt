package com.otus.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.CountPill
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusAlpha
import com.otus.designsystem.theme.OtusColors

/**
 * A two-line list row: [CompactRowText.first] and the date, then [CompactRowText.second] with its
 * marks and folders, and an optional preview. Selection works as in [ContentRow].
 *
 * [dimmed] fades the content but never the selection tint; pass false while the list is selecting,
 * so selected rows read clearly. Accessibility services read [CompactRowText.description] instead of the visible
 * text, because the marks and pills are icons.
 */
@OptIn(ExperimentalFoundationApi::class)
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
    val alpha = if (dimmed) OtusAlpha.DIMMED else 1f
    Row(
        modifier
            .fillMaxWidth()
            .selectionTint(selection)
            .semantics(mergeDescendants = true) {
                contentDescription = text.description
                stateDescription = text.stateLabel
                selection?.let { this.selected = it.selected }
            }.combinedClickable(role = Role.Button, onLongClick = onLongClick, onClick = onClick)
            // The fade's layer includes the padding, so the avatar's badge can overhang without being cut.
            .alpha(alpha)
            .padding(
                start = InboxTokens.compactPaddingStart,
                top = InboxTokens.compactPaddingVertical,
                end = InboxTokens.compactPaddingEnd,
                bottom = InboxTokens.compactPaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.itemGap),
    ) {
        SelectableLeading(selection?.selected == true, InboxTokens.compactAvatarSize, leading = leading)
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
