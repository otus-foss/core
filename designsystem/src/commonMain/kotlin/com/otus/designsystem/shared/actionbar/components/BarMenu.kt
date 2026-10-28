package com.otus.designsystem.shared.actionbar.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.otus.designsystem.shared.overlays.components.ContextMenu
import com.otus.designsystem.shared.overlays.components.MenuAction
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
                with(density) { AboveBar(ActionBarTokens.gap.roundToPx(), MenuTokens.edge.roundToPx(), end) }
            }
        ContextMenu(actions, position, { open = false }, unrolls = false)
    }
}

@Composable
internal fun rememberBarMenu(): BarMenu = remember { BarMenu() }

/**
 * Puts a menu [gap] pixels above its button, lined up with the button's [end] or start edge, and
 * below it when there is no room above. It keeps [edge] pixels inside the window.
 */
private class AboveBar(
    private val gap: Int,
    private val edge: Int,
    private val end: Boolean,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val right = end == (layoutDirection == LayoutDirection.Ltr)
        val x = if (right) anchorBounds.right - popupContentSize.width else anchorBounds.left
        val above = anchorBounds.top - gap - popupContentSize.height
        val y = if (above >= edge) above else anchorBounds.bottom + gap
        return IntOffset(
            x.coerceIn(edge, maxOf(edge, windowSize.width - popupContentSize.width - edge)),
            y.coerceIn(edge, maxOf(edge, windowSize.height - popupContentSize.height - edge)),
        )
    }
}
