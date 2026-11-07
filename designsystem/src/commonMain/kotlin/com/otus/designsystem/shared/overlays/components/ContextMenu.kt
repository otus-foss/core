package com.otus.designsystem.shared.overlays.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.overlays.elements.AtPoint
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A small menu of [actions] that opens at [at], a point inside the element it is called from, such
 * as where the user long-pressed. It leaves the screen behind it as it is. It flips above or to the
 * left of the point when it would not fit, and a tap outside it or Back runs [onDismiss].
 */
@Composable
internal fun ContextMenu(
    actions: List<UiAction>,
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
    actions: List<UiAction>,
    position: PopupPositionProvider,
    onDismiss: () -> Unit,
    unrolls: Boolean = true,
) {
    MenuPopup(position, onDismiss, unrolls = unrolls) { MenuRows(actions, onDismiss, Modifier.menuSurface()) }
}

/**
 * [actions] as menu rows that scroll once they reach [MenuTokens.dropdownMaxHeight]. A picked row
 * runs [onDismiss], then its action. [surface] draws behind the rows, inside the menu's size.
 */
@Composable
internal fun MenuRows(
    actions: List<UiAction>,
    onDismiss: () -> Unit,
    surface: Modifier = Modifier,
) {
    Column(
        Modifier
            .width(IntrinsicSize.Max)
            .widthIn(min = MenuTokens.minWidth)
            .heightIn(max = MenuTokens.dropdownMaxHeight)
            .then(surface)
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
    val shape = SmoothCornerShape(MenuTokens.radius)
    return background(OtusColors.bgDark, shape).strokeBorder(Sizes.borderWidth, OtusColors.bgLight, shape)
}

/** Calls [onPress] with the point of every press on the element, without taking the press from it. */
public fun Modifier.onPressDown(onPress: (Offset) -> Unit): Modifier =
    pointerInput(onPress) {
        awaitEachGesture {
            onPress(awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial).position)
        }
    }

@Composable
private fun MenuRow(
    action: UiAction,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(MenuTokens.radius - MenuTokens.inset)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = MenuTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = action.symbol
        if (icon != null) Symbol(icon)
        Text(action.label, style = panelTextStyle())
    }
}
