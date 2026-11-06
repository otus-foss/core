package com.otus.designsystem.shared.panels.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_left
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/**
 * The control panel's leading button. Its glyph sits at the start of the 48 dp target rather than
 * in its middle, so the chevron's stroke lines up with the page's start inset; the target's other
 * end may overlap the content after it.
 */
@Composable
public fun PanelNavigation(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: PanelNavigationIcon = PanelNavigationIcon.Back,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(Sizes.touchTarget)
            .semantics { contentDescription = description }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.controlPressFeedback(interactions)) {
            when (icon) {
                PanelNavigationIcon.Back -> {
                    Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true, size = PanelTokens.navigationIconSize)
                }

                PanelNavigationIcon.Close -> {
                    Symbol(Res.drawable.symbol_close, size = PanelTokens.closeIconSize)
                }
            }
        }
    }
}
