package com.otus.designsystem.components

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.LabelPill
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A person in a contact list: the [avatar], the [title] over a `text2` [subtitle] such as their
 * preferred address, then an optional `text2` [detail] such as "+1" for more addresses and a
 * [label] pill, such as the shared address book the card comes from. While [selected] is not null
 * the row takes part in a selection: a long press starts one, and a selected row turns accent with
 * a check in place of the avatar, as message rows do.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ContactRow(
    title: String,
    subtitle: String,
    avatar: ChipAvatar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    detail: String? = null,
    label: ContactRowLabel? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ContactTokens.rowHeight)
            .selectionTint(RowSelection(selected))
            .combinedClickable(role = Role.Button, onLongClick = onLongClick, onClick = onClick)
            .semantics { if (selected) stateDescription = "Selected" }
            .padding(horizontal = PageTokens.textInset),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectableLeading(selected, ContactTokens.avatarSize) { PersonAvatar(avatar, ContactTokens.avatarSize) }
        Column(Modifier.weight(1f)) {
            Text(title, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = SheetTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        detail?.let {
            Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
        }
        label?.let { LabelPill(it.text, icon = it.icon) }
    }
}

/** A [ContactRow]'s pill, such as the shared book "Team" with a group icon. */
public class ContactRowLabel(
    public val text: String,
    public val icon: DrawableResource? = null,
)
