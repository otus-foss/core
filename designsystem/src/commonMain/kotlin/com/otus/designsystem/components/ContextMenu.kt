package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.MenuTokens
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween
import com.otus.designsystem.theme.panelTextStyle

/**
 * A small menu of [actions] that opens at [at], a point inside the element it is called from, such
 * as where the user long-pressed. It leaves the screen behind it as it is. It flips above or to the
 * left of the point when it would not fit, and a tap outside it or Back runs [onDismiss].
 */
@Composable
public fun ContextMenu(
    actions: List<MenuAction>,
    at: Offset,
    onDismiss: () -> Unit,
) {
    val edge = with(LocalDensity.current) { MenuTokens.edge.roundToPx() }
    val position = remember(at, edge) { AtPoint(at.round(), edge) }
    MenuPopup(position, onDismiss) {
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = MenuTokens.minWidth)
                .menuSurface()
                .padding(MenuTokens.inset),
        ) {
            actions.forEach { action ->
                MenuRow(action) {
                    onDismiss()
                    action.onClick()
                }
            }
        }
    }
}

/** A popup at [position] that fades and grows in, as menus open; it takes focus while [focusable]. */
@Composable
internal fun MenuPopup(
    position: PopupPositionProvider,
    onDismiss: () -> Unit,
    focusable: Boolean = true,
    content: @Composable () -> Unit,
) {
    val shown = remember { MutableTransitionState(false).apply { targetState = true } }
    Popup(position, onDismissRequest = onDismiss, properties = PopupProperties(focusable = focusable)) {
        AnimatedVisibility(
            shown,
            enter =
                fadeIn(motionTween(MotionSpeed.Fast)) +
                    scaleIn(motionTween(MotionSpeed.Fast), initialScale = START_SCALE),
        ) { content() }
    }
}

/** The raised `bgDark` surface with a `bgLight` outline that every menu sits on. */
internal fun Modifier.menuSurface(radius: Dp = MenuTokens.radius): Modifier {
    val shape = RoundedCornerShape(radius)
    return background(OtusColors.bgDark, shape).border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
}

/** Calls [onPress] with the point of every press on the element, without taking the press from it. */
public fun Modifier.onPressDown(onPress: (Offset) -> Unit): Modifier =
    pointerInput(onPress) {
        awaitEachGesture {
            onPress(
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial).position,
            )
        }
    }

@Composable
private fun MenuRow(
    action: MenuAction,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(MenuTokens.radius - MenuTokens.inset)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = MenuTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        action.icon?.let { Symbol(it) }
        Text(action.label, style = panelTextStyle())
    }
}

/** Puts the menu's corner on the point, flipping it to stay [edge] pixels inside the window. */
private class AtPoint(
    private val point: IntOffset,
    private val edge: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left + point.x
        val y = anchorBounds.top + point.y
        val fitsRight = x + popupContentSize.width <= windowSize.width - edge
        val fitsBelow = y + popupContentSize.height <= windowSize.height - edge
        return IntOffset(
            (if (fitsRight) x else x - popupContentSize.width).coerceIn(
                edge,
                maxOf(
                    edge,
                    windowSize.width - popupContentSize.width - edge,
                ),
            ),
            (if (fitsBelow) y else y - popupContentSize.height).coerceIn(
                edge,
                maxOf(
                    edge,
                    windowSize.height - popupContentSize.height - edge,
                ),
            ),
        )
    }
}

private const val START_SCALE = 0.9f
