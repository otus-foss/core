package com.otus.designsystem.shared.actionbar.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.overlays.components.MenuPopup
import com.otus.designsystem.shared.overlays.components.menuSurface
import com.otus.designsystem.shared.overlays.elements.AbovePopup
import com.otus.designsystem.shared.overlays.elements.PopupAlign
import com.otus.designsystem.theme.tokens.overlays.MenuTokens

/**
 * A small bar of [actions] as icons that opens just above [at], a point inside the element it is
 * called from, such as where the user long-pressed. Give every action an icon; screen readers hear
 * its label. The bar moves below the point when there is no room above and stays inside the
 * screen. Choosing an action closes it first, and a tap outside it or Back runs [onDismiss].
 */
@Composable
public fun QuickActionBar(
    actions: List<UiAction>,
    at: Offset,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val edge = with(density) { MenuTokens.edge.roundToPx() }
    val gap = with(density) { MenuTokens.barGap.roundToPx() }
    val position = remember(at, edge, gap) { AbovePopup(gap, edge, PopupAlign.Center, at.round()) }
    MenuPopup(position, onDismiss) {
        Row(Modifier.menuSurface().padding(MenuTokens.inset)) {
            actions.forEach { action ->
                IconButton(
                    action.label,
                    {
                        onDismiss()
                        action.onClick()
                    },
                    look = IconButtonLook.Plain,
                ) {
                    val icon = action.symbol
                    if (icon != null) Symbol(icon)
                }
            }
        }
    }
}
