package com.otus.designsystem.shared.overlays.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.panelTextStyle

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
    ContextMenu(actions, remember(at, edge) { AtPoint(at.round(), edge) }, onDismiss)
}

/**
 * A [ContextMenu] at [position]; its rows scroll once it reaches [MenuTokens.dropdownMaxHeight].
 * It unrolls as it opens while [unrolls] is true.
 */
@Composable
internal fun ContextMenu(
    actions: List<MenuAction>,
    position: PopupPositionProvider,
    onDismiss: () -> Unit,
    unrolls: Boolean = true,
) {
    MenuPopup(position, onDismiss, unrolls = unrolls) {
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = MenuTokens.minWidth)
                .heightIn(max = MenuTokens.dropdownMaxHeight)
                .menuSurface()
                .verticalScroll(rememberScrollState())
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

/**
 * A popup at [position] that opens at full size and, when it [unrolls], unrolls from its top edge,
 * so its first rows are readable at once; otherwise it shows in the same frame. While [focusable]
 * it takes focus and closes on a tap outside; otherwise taps on the keyboard count as outside, so
 * the caller closes it.
 */
@Composable
internal fun MenuPopup(
    position: PopupPositionProvider,
    onDismiss: () -> Unit,
    focusable: Boolean = true,
    unrolls: Boolean = true,
    content: @Composable () -> Unit,
) {
    val shown = remember { MutableTransitionState(!unrolls).apply { targetState = true } }
    val properties = PopupProperties(focusable = focusable, dismissOnClickOutside = focusable)
    Popup(position, onDismissRequest = onDismiss, properties = properties) {
        AnimatedVisibility(
            shown,
            enter = expandVertically(motionTween(MotionSpeed.Fast), expandFrom = Alignment.Top),
        ) { content() }
    }
}

/** The raised `bgDark` surface with a `bgLight` outline that every menu sits on. */
internal fun Modifier.menuSurface(): Modifier {
    val shape = RoundedCornerShape(MenuTokens.radius)
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
        return IntOffset(
            flipToFit(x, popupContentSize.width, windowSize.width),
            flipToFit(y, popupContentSize.height, windowSize.height),
        )
    }

    /** Starts the menu at [at], or ends it there when its [size] would not fit before the window's end. */
    private fun flipToFit(
        at: Int,
        size: Int,
        window: Int,
    ): Int = (if (at + size <= window - edge) at else at - size).keptInside(size, window, edge)
}

/**
 * This start of a popup [size] pixels long, moved to stay [edge] pixels inside a [window] that long,
 * or to [edge] when the popup is too long for that.
 */
internal fun Int.keptInside(
    size: Int,
    window: Int,
    edge: Int,
): Int = coerceIn(edge, maxOf(edge, window - size - edge))
