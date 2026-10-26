package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.PanelTokens

/** [avatar] at [size] as a button in a 48 dp target, which screen readers announce as [label]. */
@Composable
public fun AvatarButton(
    avatar: ChipAvatar,
    size: Dp,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(maxOf(size, PanelTokens.touchTarget))
            .clip(CircleShape)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { PersonAvatar(avatar, size) }
}
