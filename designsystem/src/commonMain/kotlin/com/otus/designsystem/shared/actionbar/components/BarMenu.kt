package com.otus.designsystem.shared.actionbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.overlays.components.ContextMenu
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.shared.overlays.elements.AbovePopup
import com.otus.designsystem.shared.overlays.elements.PopupAlign
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otus.designsystem.theme.tokens.overlays.MenuTokens

/**
 * Whether a bar button's [ContextMenu] is open. [click] runs an action, or opens its menu when it
 * has one; [Menu] draws the open menu and belongs inside the button it opens from.
 */
internal class BarMenu {
    private var open by mutableStateOf(false)

    fun click(action: BarAction): () -> Unit = click(action.menu, action.onClick)

    /** Runs [run], or opens [menu] when it has choices. */
    fun click(
        menu: List<MenuAction>,
        run: () -> Unit = {},
    ): () -> Unit = { if (menu.isEmpty()) run() else open = true }

    /**
     * [action]'s open menu, one bar gap above the button and lined up with its [end] or start edge.
     * It shows at once, without unrolling, since the button's tap asked for it.
     */
    @Composable
    fun Menu(
        action: BarAction,
        end: Boolean = false,
    ) {
        Menu(action.menu, end)
    }

    /** [actions] in the open menu, placed as for an action's own. */
    @Composable
    fun Menu(
        actions: List<MenuAction>,
        end: Boolean = false,
    ) {
        if (!open) return
        val density = LocalDensity.current
        val position =
            remember(end, density) {
                with(density) {
                    AbovePopup(
                        ActionBarTokens.gap.roundToPx(),
                        MenuTokens.edge.roundToPx(),
                        if (end) PopupAlign.End else PopupAlign.Start,
                    )
                }
            }
        ContextMenu(actions, position, { open = false }, unrolls = false)
    }
}

@Composable
internal fun rememberBarMenu(): BarMenu = remember { BarMenu() }

/**
 * Makes a bar's primary one button that runs [primary], or opens its [menu], and whose description
 * names the [context] too.
 */
@Composable
internal fun Modifier.primaryButton(
    primary: BarAction,
    context: BarContext?,
    menu: BarMenu,
    shape: Shape,
): Modifier {
    val interactions = remember { MutableInteractionSource() }
    val onClick = menu.click(primary)
    return semantics { contentDescription = listOfNotNull(primary.label, context?.description).joinToString(", ") }
        .clickable(interactions, indication = null, enabled = primary.enabled, role = Role.Button, onClick = onClick)
        .controlPressFeedback(interactions, shape)
}
