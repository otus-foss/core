package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_unfold_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens

/**
 * A rounded `bgDark` card that shows what a page covers, such as "All accounts" with
 * "2 accounts · 11 folders": [avatars], [title], an optional [subtitle], and an unfold icon. The
 * whole card is one button that calls [onClick], which opens a picker such as a [ScopeSheet];
 * [onClickLabel] names that action for screen readers.
 */
@Composable
public fun ScopePicker(
    title: String,
    avatars: List<ChipAvatar>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClickLabel: String? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(PanelTokens.panelRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget)
            .clip(shape)
            .background(OtusColors.bgDark)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .clickable(
                interactions,
                indication = null,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick,
            ).controlPressFeedback(interactions, shape)
            .padding(SidebarTokens.entryPadding),
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScopeTitle(title, subtitle, avatars)
        CompositionLocalProvider(
            LocalContentColor provides OtusColors.text2,
        ) { Symbol(Res.drawable.symbol_unfold_more) }
    }
}
