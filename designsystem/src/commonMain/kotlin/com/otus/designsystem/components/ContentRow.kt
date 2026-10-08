package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors

/** A compact list row. The app supplies display text, accessibility state, and leading content. */
@Composable
public fun ContentRow(
    text: ContentRowText,
    emphasized: Boolean,
    stateLabel: String,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = stateLabel
            }.clickable(role = Role.Button, onClick = onClick)
            .padding(InboxTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(InboxTokens.itemGap),
    ) {
        leading()
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
            Text(
                text.metadata,
                fontSize = InboxTokens.metadataSize,
                color = OtusColors.text3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
