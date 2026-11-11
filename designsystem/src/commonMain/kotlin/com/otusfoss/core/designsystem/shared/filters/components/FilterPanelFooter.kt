package com.otusfoss.core.designsystem.shared.filters.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_filter_list
import com.otusfoss.core.designsystem.resources.symbol_filter_list_off
import com.otusfoss.core.designsystem.shared.actionbar.components.ActionBar
import com.otusfoss.core.designsystem.shared.actionbar.components.BarAction
import com.otusfoss.core.designsystem.shared.controls.components.PrimaryLabel
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.overlays.components.SheetHeaderAction
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.selection.FilterTokens

/**
 * A [FilterPanel]'s fixed bottom row: the accent [applyLabel] button across the panel. While
 * [counting], a small progress ring follows the label.
 */
@Composable
public fun FilterPanelFooter(
    applyLabel: String,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    applyEnabled: Boolean = true,
    counting: Boolean = false,
) {
    ActionButton(
        applyLabel,
        onApply,
        modifier.fillMaxWidth().padding(FilterTokens.footerPadding),
        primary = true,
        enabled = applyEnabled,
        labelContent = { PrimaryLabel(applyLabel, applyEnabled, counting) },
    )
}

/**
 * An [ActionBar] in place of a page's own while filters fill the page's grown control panel: Show
 * as its primary, and Reset as its one icon, greyed out while [resetEnabled] is false.
 */
@Composable
public fun FilterShowBar(
    applyLabel: String,
    onApply: () -> Unit,
    onReset: () -> Unit,
    applyEnabled: Boolean = true,
    resetEnabled: Boolean = true,
) {
    ActionBar(
        BarAction(UiAction(applyLabel, Res.drawable.symbol_filter_list, enabled = applyEnabled, onClick = onApply)),
        secondary =
            listOf(
                BarAction(
                    UiAction(
                        "Reset filters",
                        Res.drawable.symbol_filter_list_off,
                        enabled = resetEnabled,
                        onClick = onReset,
                    ),
                ),
            ),
    )
}

/** Reset at the end of a [FilterPanel]'s header, greyed out while [enabled] is false. */
@Composable
public fun FilterResetAction(
    onReset: () -> Unit,
    enabled: Boolean,
) {
    SheetHeaderAction("Reset", onReset, color = OtusColors.text2, enabled = enabled)
}
