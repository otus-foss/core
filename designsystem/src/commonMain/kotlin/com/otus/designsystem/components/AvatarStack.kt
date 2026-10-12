package com.otus.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.rememberAvatarImage
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * Up to [PanelTokens.CHIP_AVATAR_LIMIT] overlapping avatars, each outlined in [ring] so the one
 * below shows where it starts, then an optional 14 sp `text2` [label], such as a first name or
 * "+3". With a [description], screen readers read it in place of the avatars and label.
 */
@Composable
public fun AvatarStack(
    avatars: List<ChipAvatar>,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    ring: Color = OtusColors.bgRegular,
) {
    Row(
        description?.let { modifier.semantics(mergeDescendants = true) { contentDescription = it } } ?: modifier,
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-PanelTokens.chipAvatarOverlap)) {
            avatars.take(PanelTokens.CHIP_AVATAR_LIMIT).forEach { avatar ->
                Avatar(
                    avatar.initials,
                    image = rememberAvatarImage(avatar.imageBytes),
                    colorIndex = avatar.colorIndex,
                    size = PanelTokens.iconSize,
                    ring = BorderStroke(PanelTokens.borderWidth, ring),
                )
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
