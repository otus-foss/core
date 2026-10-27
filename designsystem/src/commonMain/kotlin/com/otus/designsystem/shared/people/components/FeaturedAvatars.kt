package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.AvatarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens

/**
 * A few [avatars] as large faces with their labels, two over the rest, like the Olympic rings
 * upside down, so the first two stand out. Fewer fill from the top. A label takes up to
 * [labelLines] lines, such as two for an email address.
 */
@Composable
public fun FeaturedAvatars(
    avatars: List<FeaturedAvatar>,
    modifier: Modifier = Modifier,
    labelLines: Int = 1,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        listOf(avatars.take(TOP_ROW), avatars.drop(TOP_ROW)).filter { it.isNotEmpty() }.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap)) {
                row.forEach { Face(it, labelLines) }
            }
        }
    }
}

@Composable
private fun Face(
    face: FeaturedAvatar,
    labelLines: Int,
) {
    Column(
        Modifier
            .width(AvatarTokens.featuredWidth)
            .then(face.onClick?.let { Modifier.clickable(role = Role.Button, onClick = it) } ?: Modifier),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PersonAvatar(face.avatar, AvatarTokens.featuredSize)
        Text(
            face.label,
            color = OtusColors.text2,
            textAlign = TextAlign.Center,
            maxLines = labelLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val TOP_ROW = 2
