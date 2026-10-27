package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.LabelPill
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.LabelTokens
import com.otus.designsystem.theme.OtusColors

/**
 * A compact list row. The app supplies display text, accessibility state, and leading content.
 * While the list is selecting, [selection] is set and accessibility services read it; a selected
 * row is tinted and shows a check in place of its leading content. Null means the list is not
 * selecting. [onLongClick], when given, usually starts selecting. The tint, the check, and the
 * corners animate together as selection changes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ContentRow(
    text: ContentRowText,
    emphasized: Boolean,
    stateLabel: String,
    onClick: () -> Unit,
    selection: RowSelection? = null,
    onLongClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectionTint(selection)
            .semantics {
                stateDescription = stateLabel
                selection?.let { this.selected = it.selected }
            }.combinedClickable(role = Role.Button, onLongClick = onLongClick, onClick = onClick)
            .padding(InboxTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.itemGap),
    ) {
        SelectableLeading(selection?.selected == true, InboxTokens.avatarSize, leading = leading)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(InboxTokens.detailGap)) {
            Row(horizontalArrangement = Arrangement.spacedBy(InboxTokens.detailGap)) {
                Text(
                    text.title,
                    Modifier.weight(1f),
                    fontSize = InboxTokens.subjectSize,
                    lineHeight = InboxTokens.subjectLineHeight,
                    fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text.trailingText, fontSize = InboxTokens.senderSize, color = OtusColors.text2)
            }
            Text(
                text.subtitle,
                fontSize = InboxTokens.senderSize,
                color = OtusColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            RowDetails(text)
        }
    }
}

/** Labels, then the metadata text. The line keeps a label's height, so rows with and without labels line up. */
@Composable
private fun RowDetails(text: ContentRowText) {
    Row(
        Modifier.heightIn(min = LabelTokens.height).clipToBounds(),
        horizontalArrangement = Arrangement.spacedBy(LabelTokens.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.labels.forEach { LabelPill(it.text, icon = it.icon, highlighted = it.highlighted) }
        if (text.metadata.isNotEmpty()) {
            Text(
                text.metadata,
                Modifier.weight(1f, fill = false),
                fontSize = InboxTokens.metadataSize,
                color = OtusColors.text3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
