package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.shared.controls.elements.Chip
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLabel
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.TypeScale
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A selected pill showing up to [ControlTokens.CHIP_AVATAR_LIMIT] overlapping avatars. The caller
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
        ChipLabel(label, detail = detail),
        onClick,
        modifier.semantics { contentDescription = description },
        selected = true,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefix != null) {
                Text(prefix, style = panelTextStyle(), color = OtusColors.text2, fontSize = TypeScale.label)
            }
            AvatarStack(avatars, ring = OtusColors.selectedFill)
        }
    }
}
