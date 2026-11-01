package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.components.ButtonPair
import com.otus.designsystem.shared.controls.components.PairButton
import com.otus.designsystem.shared.controls.components.PrimaryLabel
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.overlays.components.SheetHeaderAction
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otus.designsystem.theme.tokens.selection.FilterTokens

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
 * Reset beside the Show button, in place of a page's action bar and as tall as it, while filters
 * fill the page's grown control panel. Reset greys out while [resetEnabled] is false.
 */
@Composable
public fun FilterShowBar(
    applyLabel: String,
    onApply: () -> Unit,
    onReset: () -> Unit,
    applyEnabled: Boolean = true,
    resetEnabled: Boolean = true,
    counting: Boolean = false,
) {
    ButtonPair(
        PairButton("Reset", onReset, resetEnabled),
        PairButton(applyLabel, onApply, applyEnabled),
        Modifier.heightIn(min = ActionBarTokens.height),
        primaryBusy = counting,
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
