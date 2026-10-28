package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Up to [PanelTokens.CHIP_AVATAR_LIMIT] overlapping avatars, each outlined in [ring] so the one
 * below shows where it starts, then an optional 14 sp `text2` [label], such as a first name or
 * "+3". With a [description], screen readers read it in place of the avatars and label. Larger
 * avatars of [size] overlap in proportion.
 */
@Composable
public fun AvatarStack(
    avatars: List<ChipAvatar>,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    ring: Color = OtusColors.bgRegular,
    size: Dp = PanelTokens.iconSize,
) {
    val overlap = PanelTokens.chipAvatarOverlap * (size / PanelTokens.iconSize)
    Row(
        description?.let { modifier.semantics(mergeDescendants = true) { contentDescription = it } } ?: modifier,
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-overlap)) {
            avatars.take(PanelTokens.CHIP_AVATAR_LIMIT).forEach { avatar ->
                PersonAvatar(avatar, size, Modifier.border(PanelTokens.borderWidth, ring, CircleShape))
            }
        }
        label?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = PanelTokens.countSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
