package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.shared.pages.components.PageChrome
import com.otus.designsystem.shared.pages.components.pinnedChrome
import com.otus.designsystem.shared.sidebar.components.sidebarOpenZone
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Placement is independent of whether the header contains a title, input, or richer content.
 * [None] has no chip rail: the trailing action moves into the header, and the panel is only as
 * tall as its header.
 */
public enum class ChipPlacement { Below, Overlapping, None }

/**
 * Feature-independent surface. Each [chips] item needs a stable key and a pill-shaped visual. A
 * [compact] panel has a shorter header and pulls its chips up closer to it, such as while the list
 * below scrolls down; the change animates.
 */
@Composable
public fun ContextPanel(
    modifier: Modifier = Modifier,
    placement: ChipPlacement = ChipPlacement.Below,
    navigation: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    chips: @Composable ContextPanelScope.() -> Unit = {},
    compact: Boolean = false,
    content: @Composable () -> Unit,
) {
    val overlap =
        animateFloatAsState(
            targetValue = if (placement == ChipPlacement.Overlapping) 1f else 0f,
            animationSpec = motionTween(),
            label = "Panel surface expansion",
        )
    val geometry = remember { PanelLayoutState() }
    val scope = remember(geometry) { ContextPanelScope(geometry) }
    val scroll = rememberScrollState()
    val railShown = placement != ChipPlacement.None
    Box(
        modifier
            .pinnedChrome(PageChrome.ContextPanel)
            .fillMaxWidth()
            .sidebarOpenZone()
            .onGloballyPositioned(geometry::trackPanel)
            .panelSurface(geometry) { overlap.value },
    ) {
        Column {
            val inset by animateDpAsState(if (compact) 0.dp else PanelTokens.panelInset, motionTween())
            val pullUp by animateDpAsState(if (compact) PanelTokens.compactChipPull else 0.dp, motionTween())
            PanelHeader(
                Modifier.onGloballyPositioned(geometry::trackHeader),
                navigation,
                trailingAction,
                actionShown = !railShown,
                inset,
                content,
            )
            AnimatedVisibility(
                railShown,
                enter = expandVertically(motionTween(), expandFrom = Alignment.Top) + fadeIn(motionTween()),
                exit = shrinkVertically(motionTween(), shrinkTowards = Alignment.Top) + fadeOut(motionTween()),
                label = "Panel chip rail",
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pullUp(pullUp)
                        .padding(top = if (placement == ChipPlacement.Overlapping) 0.dp else PanelTokens.detachedGap)
                        .onGloballyPositioned(geometry::trackRail),
                    horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PanelChipRail(Modifier.weight(1f), geometry, scroll) { scope.chips() }
                    if (trailingAction != null) {
                        scope.item(ActionKey) { trailingAction() }
                    }
                }
            }
        }
    }
}

internal object ActionKey

/** The trailing [action] shows here while the panel has no chip rail, fading as the rail comes and goes. */
@Composable
private fun PanelHeader(
    modifier: Modifier,
    navigation: (@Composable () -> Unit)?,
    action: (@Composable () -> Unit)?,
    actionShown: Boolean,
    inset: Dp,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(
                min = PanelTokens.touchTarget + inset * 2,
            ).padding(end = PanelTokens.panelInset),
        // Top-aligned, so content that wraps to more lines grows down and Back stays on the first.
        verticalAlignment = Alignment.Top,
    ) {
        navigation?.let { Box(Modifier.padding(top = inset)) { it() } }
        Box(
            Modifier
                .weight(1f)
                .padding(
                    start = if (navigation == null) PanelTokens.panelInset else PanelTokens.iconGap,
                    top = inset,
                    bottom = inset,
                ).heightIn(min = PanelTokens.touchTarget),
            contentAlignment = Alignment.CenterStart,
        ) { ProvideTextStyle(panelTextStyle()) { content() } }
        if (action != null) {
            AnimatedVisibility(
                actionShown,
                Modifier.padding(top = inset),
                enter = fadeIn(motionTween()),
                exit = fadeOut(motionTween()),
            ) { action() }
        }
    }
}

/** Lays the content [by] higher and reports it that much shorter, so what follows moves up too. */
private fun Modifier.pullUp(by: Dp): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val shift = by.roundToPx()
        layout(placeable.width, (placeable.height - shift).coerceAtLeast(0)) { placeable.place(0, -shift) }
    }
