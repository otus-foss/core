package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.Chip
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A selected pill showing up to [PanelTokens.CHIP_AVATAR_LIMIT] overlapping avatars. The caller
 * chooses the [label], such as a first name or a "+3" count, the spoken [description], and an
 * optional [detail] shown after a divider line. A small `text2` [prefix], such as "To", comes
 * before the avatars, so pills of different people filters never look alike.
 */
@Composable
public fun AvatarStackChip(
    avatars: List<ChipAvatar>,
    label: String?,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    prefix: String? = null,
) {
    Chip(
        label,
        onClick,
        modifier.semantics { contentDescription = description },
        selected = true,
        detail = detail,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            prefix?.let {
                Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = PanelTokens.countSize)
            }
            AvatarStack(avatars, ring = OtusColors.selectedFill)
        }
    }
}
