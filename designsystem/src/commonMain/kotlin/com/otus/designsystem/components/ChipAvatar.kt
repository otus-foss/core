package com.otus.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.rememberAvatarImage

/** One person in an [AvatarStackChip]: generated initials and optional encoded image bytes. */
public class ChipAvatar(
    public val initials: String,
    public val colorIndex: Int,
    public val imageBytes: ByteArray? = null,
)

/** Draws [avatar] at [size], decoding its image once. */
@Composable
public fun PersonAvatar(
    avatar: ChipAvatar,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Avatar(
        avatar.initials,
        modifier,
        image = rememberAvatarImage(avatar.imageBytes),
        colorIndex = avatar.colorIndex,
        size = size,
    )
}
