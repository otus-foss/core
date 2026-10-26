package com.otus.designsystem.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.ToggleSwitch
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.MenuTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle
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
    Dropdown(onDismiss, alignEnd = true, width = ComposeTokens.optionsMenuWidth, unrolls = false) {
        OptionsList(items, onDismiss)
    }
}

/**
 * The rows of [items], as an [OptionsMenu] shows them, for any surface that lists options, such
 * as an [ExpandingBarGroup]. An action runs [onDone] before its own click, to close what holds it.
 */
@Composable
public fun OptionsList(
    items: List<OptionsItem>,
    onDone: () -> Unit,
) {
    Column {
        items.forEach { item ->
            when (item) {
                is OptionsItem.Action -> {
                    OptionsRow(
                        item.icon,
                        item.label,
                        item.detail,
                        Modifier
                            .then(if (item.current) Modifier.currentChoice() else Modifier)
                            .clickable(role = Role.Button) {
                                onDone()
                                item.onClick()
                            },
                    ) { if (item.opens) Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true) }
                }

                is OptionsItem.Toggle -> {
                    OptionsRow(
                        item.icon,
                        item.label,
                        item.detail,
                        Modifier.toggleable(item.checked, role = Role.Switch, onValueChange = item.onCheckedChange),
                    ) { ToggleSwitch(item.checked) }
                }

                is OptionsItem.Caption -> {
                    ListCaption(item.text)
                }

                OptionsItem.Divider -> {
                    OptionsDivider()
                }
            }
        }
    }
}

/** The current choice: a `selectedFill` fill, rounded inside the menu, read as selected. */
private fun Modifier.currentChoice(): Modifier =
    clip(RoundedCornerShape(MenuTokens.radius - MenuTokens.inset))
        .background(OtusColors.selectedFill)
        .semantics { selected = true }

/** The line between groups of options, inset like their rows. */
@Composable
internal fun OptionsDivider() {
    Box(
        Modifier
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset, vertical = PanelTokens.iconGap)
            .fillMaxWidth()
            .height(PanelTokens.borderWidth)
            .background(OtusColors.bgLight),
    )
}

@Composable
private fun OptionsRow(
    icon: DrawableResource,
    label: String,
    detail: String?,
    modifier: Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget + MenuTokens.inset * 2)
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon)
        Column(Modifier.weight(1f)) {
            Text(label, style = panelTextStyle())
            detail?.let {
                Text(it, style = panelTextStyle(), color = OtusColors.text3, fontSize = MenuTokens.detailSize)
            }
        }
        trailing()
    }
}
