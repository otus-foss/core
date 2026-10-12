package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
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
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.motionTween

/**
 * A minimized [ActionBar]: [RoundPrimary] at the bar's end, as far from the screen's end edge as
 * from its bottom edge. It stays in place while the keyboard moves. It fades and scales out like
 * [PickerSearch]'s clear button once [hidden] or when the keyboard starts opening, and back in once
 * the closing keyboard drops below it. A new primary's icon swaps in place; see [rememberPrimaryFace].
 */
@Composable
internal fun MinimizedBar(
    primary: BarAction,
    context: BarContext?,
    hidden: Boolean,
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
    val arrive = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Enter)
    val leave = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Exit)
    Box(
        modifier
            .fillMaxWidth()
            // ActionBarHost lifts its bar onto the keyboard; the button keeps its place instead.
            .graphicsLayer { translationY = keyboardLift(ime, navigation, this).toFloat() },
        contentAlignment = Alignment.CenterEnd,
    ) {
        AnimatedVisibility(
            keyboardAway && !hidden,
            Modifier.padding(end = navigationBalance()),
            enter = fadeIn(arrive) + scaleIn(arrive),
            exit = fadeOut(leave) + scaleOut(leave),
        ) {
            RoundPrimary(primary, context) {
                rememberPrimaryFace(primary).Swapping { Symbol(it.symbol, it.mirrorInRtl, ActionBarTokens.symbolSize) }
            }
        }
    }
}

/** How far the keyboard reaches above the navigation bar, in pixels. */
private fun keyboardLift(
    ime: WindowInsets,
    navigation: WindowInsets,
    density: Density,
): Int = (ime.getBottom(density) - navigation.getBottom(density)).coerceAtLeast(0)

/** How much farther the bar sits from the bottom edge than from the end edge: the navigation bar below it. */
@Composable
private fun navigationBalance(): Dp {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val navigation = WindowInsets.navigationBars
    val end =
        if (direction == LayoutDirection.Ltr) {
            navigation.getRight(density, direction)
        } else {
            navigation.getLeft(density, direction)
        }
    return with(density) { (navigation.getBottom(density) - end).coerceAtLeast(0).toDp() }
}
