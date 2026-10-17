package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.MenuTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A list that opens under the element it is called from, such as the addresses under a Send-from
 * pill. It lines up with the element's start, or its end when [alignEnd], and opens above it when
 * there is no room below. [width] fixes its width; without it the list spans the window, inside
 * the menu edge. A tap outside or Back runs [onDismiss]. A dropdown that must leave the keyboard
 * to a field, such as suggestions while typing, is not [focusable].
 */
@Composable
public fun Dropdown(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    width: Dp? = null,
    focusable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val gap = with(density) { PanelTokens.cutoutGap.roundToPx() }
    val edge = with(density) { MenuTokens.edge.roundToPx() }
    val position = remember(alignEnd, gap, edge) { BelowAnchor(alignEnd, gap, edge) }
    val window =
        with(density) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
        }
    MenuPopup(position, onDismiss, focusable) {
        Column(
            modifier
                .width(width ?: (window - MenuTokens.edge * 2))
                .heightIn(max = MenuTokens.dropdownMaxHeight)
                .menuSurface(PanelTokens.panelRadius)
                .verticalScroll(rememberScrollState())
                .selectableGroup()
                .padding(MenuTokens.inset),
            content = content,
        )
    }
}

/**
 * A choice in a [Dropdown]: an optional [leading] avatar, the [title], a smaller `text3`
 * [subtitle] under it, and a `text3` [detail] at its end, such as a size. The [selected] row is
 * filled with `bgRegular`, without a mark.
 */
@Composable
public fun DropdownRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    detail: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(PanelTokens.panelRadius - MenuTokens.inset)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget)
            .background(if (selected) OtusColors.bgRegular else OtusColors.bgDark, shape)
            .selectable(selected, onClick = onClick)
            .padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset, vertical = PanelTokens.iconGap),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.rowPadding - MenuTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(title, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Text(
                    it,
                    style = panelTextStyle(),
                    color = OtusColors.text3,
                    fontSize = ComposeTokens.detailSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        detail?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ComposeTokens.detailSize,
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
            .padding(top = MenuTokens.rowPadding - MenuTokens.inset, bottom = PanelTokens.iconGap)
            .semantics { heading() },
        style = panelTextStyle(),
        color = OtusColors.text3,
        fontSize = ComposeTokens.detailSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Puts the popup under the anchor, or above it when only that fits, [edge] pixels inside the window's sides. */
private class BelowAnchor(
    private val alignEnd: Boolean,
    private val gap: Int,
    private val edge: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val atEnd = alignEnd != (layoutDirection == LayoutDirection.Rtl)
        val x = if (atEnd) anchorBounds.right - popupContentSize.width else anchorBounds.left
        val below = anchorBounds.bottom + gap
        val above = anchorBounds.top - gap - popupContentSize.height
        val fitsBelow = below + popupContentSize.height <= windowSize.height - edge
        // Never over the anchor: with room on neither side, such as while the keyboard is up, it
        // opens below and moves once the window grows again.
        val y = if (fitsBelow || above < edge) below else above
        return IntOffset(x.coerceIn(edge, maxOf(edge, windowSize.width - popupContentSize.width - edge)), y)
    }
}
