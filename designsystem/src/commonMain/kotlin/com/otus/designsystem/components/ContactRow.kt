package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.LabelPill
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A person in a sheet, without a card: the [avatar], the [title] over a `text2` [subtitle] such
 * as their preferred address. A list of people on a page uses [ContactEntry] instead.
 */
@Composable
public fun ContactRow(
    title: String,
    subtitle: String,
    avatar: ChipAvatar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ContactTokens.rowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PageTokens.textInset),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(avatar, ContactTokens.avatarSize)
        ContactText(title, subtitle)
    }
}

/**
 * A person or group in a contact list, on an [EntryCard] as mail is in the inbox: the [avatar],
 * the [title] over a `text2` [subtitle], then an optional `text2` [detail] such as "+1" for more
 * addresses and a [label] pill, such as the shared address book the card comes from. While
 * [selection] is not null the list is selecting, and a selected entry turns accent with a check in
 * place of the avatar.
 */
@Composable
public fun ContactEntry(
    title: String,
    subtitle: String,
    avatar: ChipAvatar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selection: RowSelection? = null,
    detail: String? = null,
    label: ContactRowLabel? = null,
) {
    EntryCard(
        onClick,
        { PersonAvatar(avatar, ContactTokens.avatarSize) },
        modifier,
        selection = selection,
        onLongClick = onLongClick,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactText(title, subtitle)
        detail?.let {
            Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
        }
        label?.let { LabelPill(it.text, icon = it.icon) }
    }
}

/** A [ContactEntry]'s pill, such as the shared book "Team" with a group icon. */
public class ContactRowLabel(
    public val text: String,
    public val icon: DrawableResource? = null,
)

@Composable
private fun RowScope.ContactText(
    title: String,
    subtitle: String,
) {
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
}
