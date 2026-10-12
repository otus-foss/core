package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween

/**
 * A minimized bar's button: [primary]'s symbol in a circle as tall as the bar, filled like the
 * full bar's primary and ringed in `accentLight`. With a [context], a fully rounded `bgDark` shell
 * grows out from behind the circle toward the start on the enter curve and shows the context; it
 * shrinks back behind the circle on the exit curve. The whole button runs the primary, and its
 * description names the context too. Both follow the frontmost bar; see [rememberFrontBar].
 */
@Composable
internal fun RoundPrimary(
    primary: BarAction,
    context: BarContext?,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val pill = RoundedCornerShape(percent = 50)
    val front = rememberFrontBar(primary, context)
    Box(
        modifier
            .height(ActionBarTokens.height)
            .semantics { contentDescription = listOfNotNull(primary.label, context?.description).joinToString(", ") }
            .clickable(interactions, null, enabled = primary.enabled, role = Role.Button, onClick = primary.onClick)
            .controlPressFeedback(interactions, pill),
        contentAlignment = Alignment.CenterEnd,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (primary.enabled) OtusColors.text1 else OtusColors.text3,
        ) {
            ContextShell(front, primary.enabled, pill)
            Circle(primary.enabled) {
                front.face.Swapping { Symbol(it.symbol, it.mirrorInRtl, ActionBarTokens.symbolSize) }
            }
        }
    }
}

/**
 * The `bgDark` [pill] behind the circle that shows [front]'s context. It grows out from the
 * circle's width toward the start on the enter curve and shrinks back on the exit curve.
 */
@Composable
private fun ContextShell(
    front: FrontBar,
    enabled: Boolean,
    pill: Shape,
) {
    val shown = front.context
    // The last context and the title it went with stay drawn while the shell shrinks away.
    val last = remember { LastContext() }
    val title = remember { LastTitle() }
    if (shown != null) {
        last.value = shown
        title.value = front.face.targetState.label
    }
    val visible = remember { MutableTransitionState(shown != null) }
    visible.targetState = shown != null
    val circleWidth = with(LocalDensity.current) { ActionBarTokens.height.roundToPx() }
    val grow = motionTween<IntSize>(MotionSpeed.Normal, MotionCurve.Enter)
    val shrink = motionTween<IntSize>(MotionSpeed.Normal, MotionCurve.Exit)
    val appear = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Enter)
    val vanish = motionTween<Float>(MotionSpeed.Fast, MotionCurve.Exit)
    AnimatedVisibility(
        visible,
        // Drawn on the resizing container, so the round end follows the width as it changes.
        Modifier
            .fillMaxHeight()
            .background(OtusColors.bgDark, pill)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, pill),
        enter = expandHorizontally(grow, Alignment.End) { circleWidth },
        exit = shrinkHorizontally(shrink, Alignment.End) { circleWidth },
    ) {
        Box(
            Modifier.fillMaxHeight().animateEnterExit(enter = fadeIn(appear), exit = fadeOut(vanish)),
            contentAlignment = Alignment.CenterStart,
        ) {
            last.value?.let {
                ContextContent(
                    title.value,
                    it,
                    if (enabled) OtusColors.text2 else OtusColors.text3,
                    // The shell runs on behind the circle, so its round end sits under it.
                    Modifier.padding(
                        start = ActionBarTokens.contextInset,
                        end = ActionBarTokens.height + ActionBarTokens.tokenGap,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Circle(
    enabled: Boolean,
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(ActionBarTokens.height)
            .background(if (enabled) OtusColors.accentDark else OtusColors.bgDark, CircleShape)
            .border(
                PanelTokens.borderWidth,
                if (enabled) OtusColors.accentLight else OtusColors.strokeBgDark,
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) { icon() }
}

/** The title shown beside the last context, kept like [LastContext] while the shell shrinks away. */
private class LastTitle {
    var value: String = ""
}
