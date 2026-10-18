package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/** A small pill with a [person]'s avatar and name inside text, such as a quote's author. */
@Composable
internal fun PersonPill(
    person: NamedAvatar,
    onClick: (() -> Unit)?,
) {
    Row(
        Modifier
            .height(ReadTokens.quotePillHeight)
            .background(OtusColors.bgDark, CircleShape)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = ReadTokens.quotePillStart, end = PanelTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(ReadTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(person.avatar, ReadTokens.quoteAvatarSize)
        Text(
            person.name,
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = ReadTokens.quoteTextSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
