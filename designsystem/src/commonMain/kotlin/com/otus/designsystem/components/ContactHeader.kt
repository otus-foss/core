package com.otus.designsystem.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The top of a contact's page: a large [avatar] and, under it, `text2` [lines] such as the
 * person's title and organization. The name sits in the page's panel, so it is not repeated here.
 * With [photoChoices], such as Choose photo and Remove photo, the avatar wears a pen badge and
 * opens them in a menu; without them, a photo opens large on a tap.
 */
@Composable
public fun ContactHeader(
    avatar: ChipAvatar,
    lines: List<String>,
    modifier: Modifier = Modifier,
    photoChoices: List<OptionsItem> = emptyList(),
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(ContactTokens.headerGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (photoChoices.isEmpty()) {
            ExpandingAvatar(avatar, ContactTokens.headerAvatarSize)
        } else {
            PhotoButton(avatar, photoChoices)
        }
        lines.forEach { line ->
            Text(line, style = panelTextStyle(), color = OtusColors.text2, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PhotoButton(
    avatar: ChipAvatar,
    choices: List<OptionsItem>,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Box(
            Modifier
                .clickable(role = Role.Button) { open = true }
                .semantics { contentDescription = "Change photo" },
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
        if (open) OptionsMenu(choices) { open = false }
    }
}
