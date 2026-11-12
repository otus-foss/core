package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.people.PersonSheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A line that leads to the people someone shares threads with: their faces, a [text] that names
 * them, such as "Also in threads with Ben and Priya", and a chevron.
 */
@Composable
public fun SharedPeopleRow(
    avatars: List<ChipAvatar>,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The text names them, so screen readers skip the faces.
        AvatarStack(
            avatars,
            Modifier.clearAndSetSemantics {},
            ring = OtusColors.bgDark,
            size = PersonSheetTokens.faceSize,
        )
        Text(
            text,
            Modifier.weight(1f),
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = SheetTokens.detailSize,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
        }
    }
}
