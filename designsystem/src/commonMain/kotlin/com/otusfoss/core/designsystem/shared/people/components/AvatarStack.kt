package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.TypeScale
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * Up to [ControlTokens.CHIP_AVATAR_LIMIT] overlapping avatars, each outlined in [ring] so the one
 * below shows where it starts, then an optional 14 sp `text2` [label], such as a first name or
 * "+3". With a [description], screen readers read it in place of the avatars and label. Larger
 * avatars of [size] overlap in proportion.
 */
@Composable
internal fun AvatarStack(
    avatars: List<ChipAvatar>,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    ring: Color = OtusColors.bgRegular,
    size: Dp = Sizes.iconSize,
) {
    val overlap = ControlTokens.chipAvatarOverlap * (size / Sizes.iconSize)
    Row(
        description?.let { modifier.semantics(mergeDescendants = true) { contentDescription = it } } ?: modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-overlap)) {
            avatars.take(ControlTokens.CHIP_AVATAR_LIMIT).forEach { avatar ->
                PersonAvatar(avatar, size, Modifier.border(Sizes.borderWidth, ring, CircleShape))
            }
        }
        if (label != null) {
            Text(
                label,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = TypeScale.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * [avatars] in a `bgLight` ring beside a title that names them, hidden from screen readers, since
 * their initials would only repeat the title.
 */
@Composable
internal fun SilentAvatarStack(
    avatars: List<ChipAvatar>,
    size: Dp,
) {
    AvatarStack(avatars, Modifier.clearAndSetSemantics {}, ring = OtusColors.bgLight, size = size)
}
