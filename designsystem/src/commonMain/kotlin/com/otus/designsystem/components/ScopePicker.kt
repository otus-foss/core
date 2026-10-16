package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_unfold_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens

/**
 * A compact button for a page header's action slot that shows whose content the page covers, such
 * as every account's: their [avatars] and an unfold icon. It calls [onClick], which opens a picker
 * such as a [ChoiceSheet]; [label] tells screen readers what is shown, such as "All accounts".
 */
@Composable
public fun ScopePicker(
    avatars: List<ChipAvatar>,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Row(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .clickable(interactions, indication = null, onClickLabel = "Choose", role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, RoundedCornerShape(PanelTokens.panelRadius))
            .padding(horizontal = SidebarTokens.entryPadding),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The label names who is shown; initials would only repeat it.
        AvatarStack(avatars, Modifier.clearAndSetSemantics {}, ring = OtusColors.bgLight)
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_unfold_more)
        }
    }
}
