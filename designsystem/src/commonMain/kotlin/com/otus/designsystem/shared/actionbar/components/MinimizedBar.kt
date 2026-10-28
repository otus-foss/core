package com.otus.designsystem.shared.actionbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * A minimized [ActionBar]: [RoundPrimary] at the bar's end, as far from the screen's end edge as
 * from its bottom edge. It stays in place while the keyboard moves. It fades and scales out like
 * [PickerSearch][com.otus.designsystem.shared.selection.components.PickerSearch]'s clear button once [hidden] or
 * when the keyboard starts opening, and back in once
 * the closing keyboard drops below it. A new primary's icon swaps in place; see [rememberFrontBar].
 * With [more], a [BarMoreButton] at the bar's start opens them; it scales in when they arrive.
 */
@Composable
internal fun MinimizedBar(
    primary: BarAction,
    context: BarContext?,
    hidden: Boolean,
    more: List<MenuAction>,
    modifier: Modifier = Modifier,
) {
    val ime = WindowInsets.ime
    val navigation = WindowInsets.navigationBars
    val density = LocalDensity.current
    var keyboardAway by remember { mutableStateOf(true) }
    LaunchedEffect(ime, navigation, density) {
        val below = with(density) { (ActionBarTokens.barInset + ActionBarTokens.height).roundToPx() }
        var last = 0
        snapshotFlow { keyboardLift(ime, navigation, density) }.collect { lift ->
            keyboardAway = lift <= last && lift <= below
            last = lift
        }
    }
    val shown = keyboardAway && !hidden
    Box(
        modifier
            .fillMaxWidth()
            // ActionBarHost lifts its bar onto the keyboard; the buttons keep their places instead.
            .graphicsLayer { translationY = keyboardLift(ime, navigation, this).toFloat() },
    ) {
        Appearing(
            shown && more.isNotEmpty(),
            Modifier.align(Alignment.CenterStart).padding(start = navigationBalance(end = false)),
            scaleInFirst = true,
        ) { BarMoreButton(more) }
        Appearing(shown, Modifier.align(Alignment.CenterEnd).padding(end = navigationBalance(end = true))) {
            RoundPrimary(primary, context)
        }
    }
}

/**
 * Fades and scales [content] in and out like
 * [PickerSearch][com.otus.designsystem.shared.selection.components.PickerSearch]'s clear button. With
 * [scaleInFirst] it
 * also scales in when it first appears, such as when a selection's bar replaces the list's.
 */
@Composable
private fun Appearing(
    visible: Boolean,
    modifier: Modifier,
    scaleInFirst: Boolean = false,
    content: @Composable () -> Unit,
) {
    val state = remember { MutableTransitionState(visible && !scaleInFirst) }
    state.targetState = visible
    val arrive = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Enter)
    val leave = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Exit)
    AnimatedVisibility(
        state,
        modifier,
        enter = fadeIn(arrive) + scaleIn(arrive),
        exit = fadeOut(leave) + scaleOut(leave),
    ) { content() }
}

/** How far the keyboard reaches above the navigation bar, in pixels. */
private fun keyboardLift(
    ime: WindowInsets,
    navigation: WindowInsets,
    density: Density,
): Int = (ime.getBottom(density) - navigation.getBottom(density)).coerceAtLeast(0)

/**
 * How much farther the bar sits from the bottom edge than from the [end] or start edge: the
 * navigation bar below it.
 */
@Composable
private fun navigationBalance(end: Boolean): Dp {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val navigation = WindowInsets.navigationBars
    val right = end == (direction == LayoutDirection.Ltr)
    val side = if (right) navigation.getRight(density, direction) else navigation.getLeft(density, direction)
    return with(density) { (navigation.getBottom(density) - side).coerceAtLeast(0).toDp() }
}
