package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
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
 * The menu under a ⋯ button, aligned with its end, [width] wide and [gap] away: icon rows that
 * act, open more, or switch an option. Call it inside the button's layout, or a larger one such
 * as the bar the button is in, so it opens there.
 */
@Composable
public fun OptionsMenu(
    items: List<OptionsItem>,
    width: Dp = ComposeTokens.optionsMenuWidth,
    gap: Dp = PanelTokens.cutoutGap,
    onDismiss: () -> Unit,
) {
    Dropdown(onDismiss, alignEnd = true, width = width, gap = gap) {
        items.forEach { item ->
            when (item) {
                is OptionsItem.Action -> {
                    OptionsRow(
                        item.icon,
                        item.label,
                        Modifier.clickable(role = Role.Button) {
                            onDismiss()
                            item.onClick()
                        },
                    ) { if (item.opens) Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true) }
                }

                is OptionsItem.Toggle -> {
                    OptionsRow(
                        item.icon,
                        item.label,
                        Modifier.toggleable(item.checked, role = Role.Switch, onValueChange = item.onCheckedChange),
                    ) { ToggleSwitch(item.checked) }
                }

                OptionsItem.Divider -> {
                    Box(
                        Modifier
                            .padding(
                                horizontal = MenuTokens.rowPadding - MenuTokens.inset,
                                vertical = PanelTokens.iconGap,
                            ).fillMaxWidth()
                            .height(PanelTokens.borderWidth)
                            .background(OtusColors.bgLight),
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionsRow(
    icon: DrawableResource,
    label: String,
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
        Box(Modifier.weight(1f)) { Text(label, style = panelTextStyle()) }
        trailing()
    }
}
