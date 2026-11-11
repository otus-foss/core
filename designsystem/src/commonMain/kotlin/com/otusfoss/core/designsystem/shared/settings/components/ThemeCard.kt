package com.otusfoss.core.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_check_circle
import com.otusfoss.core.designsystem.resources.symbol_more_horiz
import com.otusfoss.core.designsystem.shared.controls.elements.IconButton
import com.otusfoss.core.designsystem.shared.controls.elements.IconButtonLook
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.overlays.components.ContextMenu
import com.otusfoss.core.designsystem.shared.overlays.components.onPressDown
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.settings.ThemeTokens

/**
 * A theme to pick: [sketch]'s colors as rings above [title]. The
 * selected card has an accent ring and a check; the card is one radio button.
 * With [actions], a long press opens them in a [ContextMenu] where the finger is, and the selected
 * card shows a more button that opens them in place of the check.
 */
@Composable
public fun ThemeCard(
    title: String,
    sketch: OtusPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<UiAction> = emptyList(),
) {
    var press by remember { mutableStateOf(Offset.Zero) }
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    val openMenu = { menuAt = press }.takeIf { actions.isNotEmpty() }
    val shape = SmoothCornerShape(ThemeTokens.cardRadius)
    val interactions = remember { MutableInteractionSource() }
    // The menu opens in this box, which has no padding, so a press point maps onto it directly.
    Box(modifier.onPressDown { press = it }) {
        Column(
            Modifier
                .semantics { this.selected = selected }
                .combinedClickable(
                    interactions,
                    indication = null,
                    role = Role.RadioButton,
                    onLongClickLabel = openMenu?.let { "Theme actions" },
                    onLongClick = openMenu,
                    onClick = onClick,
                ).controlPressFeedback(interactions, shape)
                .background(OtusColors.bgDark, shape)
                .strokeBorder(
                    if (selected) Sizes.borderWidth * 2 else Sizes.borderWidth,
                    if (selected) OtusColors.text1 else OtusColors.strokeBgDark,
                    shape,
                ).padding(ThemeTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
        ) {
            ThemeRings(sketch)
            CardLabel(title, selected, openMenu)
        }
        val menuPosition = menuAt
        if (menuPosition != null) ContextMenu(actions, menuPosition) { menuAt = null }
    }
}

/** The title, then the check or, with [onMenu], the more button on a selected card. */
@Composable
private fun CardLabel(
    title: String,
    selected: Boolean,
    onMenu: (() -> Unit)?,
) {
    Row(
        Modifier.padding(horizontal = Spacing.iconGap).padding(bottom = Spacing.iconGap),
        horizontalArrangement = Arrangement.spacedBy(Spacing.inlineGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = ThemeTokens.cardTitleSize,
            lineHeight = ThemeTokens.cardTitleLineHeight,
            fontWeight = FontWeight.SemiBold,
        )
        when {
            selected && onMenu != null -> MoreButton(title, onMenu)
            selected -> SelectedCheck()
        }
    }
}

@Composable
private fun SelectedCheck() {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text1) { Symbol(Res.drawable.symbol_check_circle) }
}

/** A more button the size of the check it replaces, so the card keeps its height; its touch target overflows. */
@Composable
private fun MoreButton(
    title: String,
    onClick: () -> Unit,
) {
    Box(Modifier.size(Sizes.iconSize), contentAlignment = Alignment.Center) {
        IconButton(
            "Actions for $title",
            onClick,
            Modifier.requiredSize(Sizes.touchTarget),
            look = IconButtonLook.Plain,
        ) {
            Symbol(Res.drawable.symbol_more_horiz)
        }
    }
}
