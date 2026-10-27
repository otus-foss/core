package com.otus.designsystem.shared.sidebar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_settings
import com.otus.designsystem.resources.symbol_unfold_more
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.AvatarStack
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The [Sidebar]'s header: [avatars], [title], and an optional [subtitle], then an unfold button
 * and Settings. Tapping the title or the unfold button calls [onPick], which opens a picker such
 * as a [ScopeSheet][com.otus.designsystem.shared.selection.components.ScopeSheet]; [pickLabel] names that action
 * for screen readers. It shares the context
 * panel's header height, so the title lines up with the page title.
 */
@Composable
public fun SidebarHeader(
    title: String,
    avatars: List<ChipAvatar>,
    pickLabel: String,
    onPick: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(PanelTokens.headerHeight)
            .padding(horizontal = SidebarTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderTitle(title, subtitle, avatars, pickLabel, onPick, Modifier.weight(1f))
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            IconButton(pickLabel, onPick, look = IconButtonLook.Plain) { Symbol(Res.drawable.symbol_unfold_more) }
            IconButton("Settings", onSettings, look = IconButtonLook.Plain) { Symbol(Res.drawable.symbol_settings) }
        }
    }
}

@Composable
private fun HeaderTitle(
    title: String,
    subtitle: String?,
    avatars: List<ChipAvatar>,
    pickLabel: String,
    onPick: () -> Unit,
    modifier: Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    Row(
        modifier
            .fillMaxHeight()
            .padding(vertical = SidebarTokens.inset)
            .clickable(interactions, indication = null, onClickLabel = pickLabel, role = Role.Button, onClick = onPick)
            .clip(shape)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.entryPadding),
    ) { ScopeTitle(title, subtitle, avatars) }
}

/**
 * [avatars], then a SemiBold [title] over an optional `text2` [subtitle], as in the [SidebarHeader].
 * Place it in a row spaced by [SidebarTokens.entryPadding].
 */
@Composable
internal fun RowScope.ScopeTitle(
    title: String,
    subtitle: String?,
    avatars: List<ChipAvatar>,
) {
    if (avatars.isNotEmpty()) {
        // The title names the scope; initials would only repeat it.
        AvatarStack(
            avatars,
            Modifier.clearAndSetSemantics {},
            ring = OtusColors.bgLight,
            size = SidebarTokens.headerAvatar,
        )
    }
    Column(Modifier.weight(1f)) {
        Text(
            title,
            Modifier.semantics { heading() },
            style = panelTextStyle(),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        subtitle?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = SidebarTokens.detailSize,
                lineHeight = SidebarTokens.detailLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
