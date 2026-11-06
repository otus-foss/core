package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.shared.controls.elements.optionalButton
import com.otus.designsystem.theme.tokens.core.Sizes

/**
 * [avatar] at [size] as a button in a 48 dp target, which screen readers announce as [label]. A null
 * [onClick] shows the avatar in the same target without a button.
 */
@Composable
public fun AvatarButton(
    avatar: ChipAvatar,
    size: Dp,
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(maxOf(size, Sizes.touchTarget))
            .clip(CircleShape)
            .optionalButton(onClick, label),
        contentAlignment = Alignment.Center,
    ) { PersonAvatar(avatar, size) }
}
