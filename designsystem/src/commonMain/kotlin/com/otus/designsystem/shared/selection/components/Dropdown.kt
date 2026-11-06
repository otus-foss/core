package com.otus.designsystem.shared.selection.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.otus.designsystem.shared.overlays.components.MenuPopup
import com.otus.designsystem.shared.overlays.components.menuSurface
import com.otus.designsystem.shared.overlays.elements.PopupAlign
import com.otus.designsystem.shared.overlays.elements.keptInside
import com.otus.designsystem.shared.overlays.elements.popupStart
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A list that opens under the element it is called from, such as the addresses under a Send-from
 * pill. It lines up with the element's start, or its end when [alignEnd], and opens above it when
 * there is no room below, and scrolls when it is taller than the room it has, such as above the
 * keyboard. [width] fixes its width; without it the list spans the window, inside
 * the menu edge. A tap outside or Back runs [onDismiss]. A dropdown that must leave the keyboard
 * to a field, such as suggestions while typing, is not [focusable]: it stays open while the user
 * types, and the field closes it. It [unrolls] as it opens, unless a ⋯ button opens it, and its
 * height animates as its rows change. A [header], such as a search field, stays at the top while
 * the rows under it scroll.
 */
@Composable
public fun Dropdown(
    onDismiss: () -> Unit,
    alignEnd: Boolean = false,
    width: Dp? = null,
    focusable: Boolean = true,
    unrolls: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val gap = with(density) { PanelTokens.cutoutGap.roundToPx() }
    val edge = with(density) { MenuTokens.edge.roundToPx() }
    val minRoom = with(density) { MenuTokens.dropdownMinRoom.roundToPx() }
    val position = remember(alignEnd, gap, edge, minRoom) { BelowAnchor(alignEnd, gap, edge, minRoom) }
    val room = with(density) { position.room.toDp() }
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val window = with(density) { windowWidth.toDp() }
    MenuPopup(position, onDismiss, focusable, unrolls) {
        Column(
            Modifier
                .width(width ?: (window - MenuTokens.edge * 2))
                .heightIn(max = minOf(MenuTokens.dropdownMaxHeight, room))
                .animateContentSize(motionTween(MotionSpeed.Fast))
                .menuSurface(),
        ) {
            header?.let {
                Box(
                    Modifier.padding(start = MenuTokens.inset, top = MenuTokens.inset, end = MenuTokens.inset),
                ) { it() }
            }
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .selectableGroup()
                    .padding(MenuTokens.inset),
                content = content,
            )
        }
    }
}

/** A small `text3` heading over a group of rows in a dropdown or a list, such as an account's name. */
@Composable
public fun ListCaption(text: String) {
    Text(
        text,
        Modifier
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset)
            .padding(top = MenuTokens.rowPadding - MenuTokens.inset, bottom = Spacing.iconGap)
            .semantics { heading() },
        style = panelTextStyle(),
        color = OtusColors.text3,
        fontSize = TypeScale.detail,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Puts the popup under the anchor, or above it when only that fits, [edge] pixels inside the window's
 * sides. [room] is the height the popup may take: the space below while it holds at least [minRoom],
 * else the larger side.
 */
private class BelowAnchor(
    private val alignEnd: Boolean,
    private val gap: Int,
    private val edge: Int,
    private val minRoom: Int,
) : PopupPositionProvider {
    var room by mutableIntStateOf(Int.MAX_VALUE)
        private set

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val roomBelow = windowSize.height - edge - anchorBounds.bottom - gap
        val roomAbove = anchorBounds.top - gap - edge
        room = if (roomBelow >= minRoom) roomBelow else maxOf(roomBelow, roomAbove)
        val align = if (alignEnd) PopupAlign.End else PopupAlign.Start
        val x = anchorBounds.popupStart(align, popupContentSize.width, layoutDirection)
        val below = anchorBounds.bottom + gap
        val above = anchorBounds.top - gap - popupContentSize.height
        val fitsBelow = below + popupContentSize.height <= windowSize.height - edge
        // Never over the anchor: with room on neither side, such as while the keyboard is up, it
        // opens below and moves once the window grows again.
        val y = if (fitsBelow || above < edge) below else above
        return IntOffset(x.keptInside(popupContentSize.width, windowSize.width, edge), y)
    }
}
