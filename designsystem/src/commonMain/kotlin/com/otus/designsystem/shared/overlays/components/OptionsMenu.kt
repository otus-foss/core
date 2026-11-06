package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.ToggleSwitch
import com.otus.designsystem.shared.selection.components.Dropdown
import com.otus.designsystem.shared.selection.components.ListCaption
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The menu under a ⋯ button, aligned with its end: icon rows that act, open more, or switch an
 * option. Call it inside the button's layout, so it opens there. It shows at once, without
 * unrolling, so a tap on ⋯ answers in the same frame.
 */
@Composable
public fun OptionsMenu(
    items: List<OptionsItem>,
    onDismiss: () -> Unit,
) {
    Dropdown(onDismiss, alignEnd = true, width = MenuTokens.optionsMenuWidth, unrolls = false) {
        OptionsList(items, onDismiss)
    }
}

/**
 * The rows of [items], as an [OptionsMenu] shows them, for any surface that lists options, such
 * as an [ExpandingBarGroup][com.otus.designsystem.shared.actionbar.components.ExpandingBarGroup].
 * An action runs [onDone] before its own click, to close what holds it.
 */
@Composable
public fun OptionsList(
    items: List<OptionsItem>,
    onDone: () -> Unit,
) {
    Column {
        items.forEach { item ->
            when (item) {
                is OptionsItem.Action -> ActionOption(item, onDone)
                is OptionsItem.Toggle -> ToggleOption(item)
                is OptionsItem.Caption -> ListCaption(item.text)
                OptionsItem.Divider -> OptionsDivider()
            }
        }
    }
}

@Composable
private fun ActionOption(
    item: OptionsItem.Action,
    onDone: () -> Unit,
) {
    OptionsRow(
        item.action.symbol,
        item.action.label,
        item.detail,
        Modifier
            .currentChoice(item.current)
            .clickable(role = Role.Button) {
                onDone()
                item.action.onClick()
            },
    ) { if (item.opens) Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true) }
}

@Composable
private fun ToggleOption(item: OptionsItem.Toggle) {
    OptionsRow(
        item.icon,
        item.label,
        item.detail,
        Modifier.toggleable(item.checked, role = Role.Switch, onValueChange = item.onCheckedChange),
    ) { ToggleSwitch(item.checked) }
}

/** The current choice: a `selectedFill` fill, rounded inside the menu, read as selected. */
private fun Modifier.currentChoice(current: Boolean): Modifier =
    if (current) {
        clip(SmoothCornerShape(MenuTokens.radius - MenuTokens.inset))
            .background(OtusColors.selectedFill)
            .semantics { selected = true }
    } else {
        this
    }

/** The line between groups of options, inset like their rows. */
@Composable
internal fun OptionsDivider() {
    Box(
        Modifier
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset, vertical = Spacing.iconGap)
            .fillMaxWidth()
            .height(Sizes.borderWidth)
            .background(OtusColors.bgLight),
    )
}

@Composable
private fun OptionsRow(
    icon: DrawableResource?,
    label: String,
    detail: String?,
    modifier: Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget + MenuTokens.inset * 2)
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Symbol(icon)
        Column(Modifier.weight(1f)) {
            Text(label, style = panelTextStyle())
            if (detail != null) {
                Text(detail, style = panelTextStyle(), color = OtusColors.text3, fontSize = MenuTokens.detailSize)
            }
        }
        trailing()
    }
}
