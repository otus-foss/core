package com.otus.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.PopupPositionProvider
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.MenuTokens

/**
 * A small bar of [actions] as icons that opens just above [at], a point inside the element it is
 * called from, such as where the user long-pressed. Give every action an icon; screen readers hear
 * its label. The bar moves below the point when there is no room above and stays inside the
 * screen. Choosing an action closes it first, and a tap outside it or Back runs [onDismiss].
 */
@Composable
public fun QuickActionBar(
    actions: List<MenuAction>,
    at: Offset,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val edge = with(density) { MenuTokens.edge.roundToPx() }
    val gap = with(density) { MenuTokens.barGap.roundToPx() }
    val position = remember(at, edge, gap) { AbovePoint(at.round(), edge, gap) }
    MenuPopup(position, onDismiss) {
        Row(Modifier.menuSurface().padding(MenuTokens.inset)) {
            actions.forEach { action ->
                IconButton(
                    action.label,
                    {
                        onDismiss()
                        action.onClick()
                    },
                    outlined = false,
                ) { action.icon?.let { Symbol(it) } }
            }
        }
    }
}

/** Centers the bar over the point, [gap] pixels above it, or below when it would leave the window. */
private class AbovePoint(
    private val point: IntOffset,
    private val edge: Int,
    private val gap: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left + point.x - popupContentSize.width / 2
        val y = anchorBounds.top + point.y
        val above = y - gap - popupContentSize.height
        return IntOffset(
            x.coerceIn(edge, maxOf(edge, windowSize.width - popupContentSize.width - edge)),
            if (above >= edge) above else y + gap,
        )
    }
}
