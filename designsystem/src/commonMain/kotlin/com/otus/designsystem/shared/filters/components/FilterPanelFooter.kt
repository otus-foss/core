package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.components.ButtonPair
import com.otus.designsystem.shared.controls.components.PairButton
import com.otus.designsystem.theme.tokens.selection.FilterTokens

/**
 * A [FilterPanel]'s fixed bottom row: Reset, greyed out while [resetEnabled] is false, and the
 * primary [applyLabel] in one [ButtonPair]. While [counting], a small progress ring follows the
 * label.
 */
@Composable
public fun FilterPanelFooter(
    applyLabel: String,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    applyEnabled: Boolean = true,
    resetEnabled: Boolean = true,
    counting: Boolean = false,
) {
    ButtonPair(
        PairButton("Reset", onReset, resetEnabled),
        PairButton(applyLabel, onApply, applyEnabled),
        modifier.padding(FilterTokens.footerPadding),
        primaryBusy = counting,
    )
}
