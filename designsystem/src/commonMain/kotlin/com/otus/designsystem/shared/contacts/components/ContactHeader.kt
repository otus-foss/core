package com.otus.designsystem.shared.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.ExpandingAvatar
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The top of a contact's page: a large [avatar] and, under it, `text2` [lines] such as the
 * person's title and organization. The name sits in the page's panel, so it is not repeated here.
 * With [onEdit], the avatar wears a pen badge and a tap runs it, such as to open an avatar editor;
 * without it, a photo opens large on a tap.
 */
@Composable
public fun ContactHeader(
    avatar: ChipAvatar,
    lines: List<String>,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(ContactTokens.headerGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (onEdit == null) {
            ExpandingAvatar(avatar, ContactTokens.headerAvatarSize)
        } else {
            EditableAvatar(avatar, onEdit)
        }
        lines.forEach { line ->
            Text(line, style = panelTextStyle(), color = OtusColors.text2, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EditableAvatar(
    avatar: ChipAvatar,
    onEdit: () -> Unit,
) {
    Box(
        Modifier
            .clickable(onClickLabel = "Edit avatar", role = Role.Button, onClick = onEdit),
        contentAlignment = Alignment.BottomEnd,
    ) {
        PersonAvatar(avatar, ContactTokens.headerAvatarSize)
        Box(
            Modifier
                .size(ContactTokens.photoBadgeSize)
                .background(OtusColors.bgRegular, CircleShape)
                .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
                Symbol(Res.drawable.symbol_edit, size = ContactTokens.photoBadgeIconSize)
            }
        }
    }
}
