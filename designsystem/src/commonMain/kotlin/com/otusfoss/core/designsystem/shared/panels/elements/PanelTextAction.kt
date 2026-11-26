package com.otusfoss.core.designsystem.shared.panels.elements

import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.elements.TextAction
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A text button for a control panel's trailing action, such as "Select all". Its label ends at
 * the page's side inset, level with the chevron's start and the rows' edges.
 */
@Composable
public fun PanelTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideTextStyle(panelTextStyle()) { TextAction(label, onClick, PanelTokens.textActionInset, modifier) }
}
