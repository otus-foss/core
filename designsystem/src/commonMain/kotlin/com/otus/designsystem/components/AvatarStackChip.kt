package com.otus.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.Chip
import com.otus.designsystem.elements.rememberAvatarImage
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

/**
 * A selected pill showing up to [PanelTokens.CHIP_AVATAR_LIMIT] overlapping avatars. The caller
 * chooses the [label], such as a first name or a "+3" count, the spoken [description], and an
 * optional [detail] shown after a divider line.
 */
@Composable
public fun AvatarStackChip(
    avatars: List<ChipAvatar>,
    label: String?,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    Chip(
        label,
        onClick,
        modifier.semantics { contentDescription = description },
        selected = true,
        detail = detail,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-PanelTokens.chipAvatarOverlap)) {
            avatars.take(PanelTokens.CHIP_AVATAR_LIMIT).forEach { StackedAvatar(it) }
        }
    }
}

@Composable
private fun StackedAvatar(avatar: ChipAvatar) {
    Avatar(
        avatar.initials,
        image = rememberAvatarImage(avatar.imageBytes),
        colorIndex = avatar.colorIndex,
        size = PanelTokens.iconSize,
        ring = BorderStroke(PanelTokens.borderWidth, OtusColors.accentLight),
    )
}
