package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween
import com.otus.designsystem.theme.panelTextStyle

/**
 * Placement is independent of whether the header contains a title, input, or richer content.
 * [None] has no chip rail: the trailing action moves into the header, and the panel is only as
 * tall as its header.
 */
public enum class ChipPlacement { Below, Overlapping, None }

/** Feature-independent surface. Each [chips] item needs a stable key and a pill-shaped visual. */
@Composable
public fun ContextPanel(
    modifier: Modifier = Modifier,
    placement: ChipPlacement = ChipPlacement.Below,
    navigation: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    chips: @Composable ContextPanelScope.() -> Unit = {},
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
            .fillMaxWidth()
            .sidebarOpenZone()
            .onGloballyPositioned(geometry::trackPanel)
            .panelSurface(geometry) { overlap.value },
    ) {
        Column {
            PanelHeader(
                Modifier.onGloballyPositioned(geometry::trackHeader),
                navigation,
                trailingAction,
                actionShown = !railShown,
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
    content: @Composable () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = PanelTokens.headerHeight).padding(end = PanelTokens.panelInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigation?.invoke()
        Box(
            Modifier
                .weight(1f)
                .padding(
                    start = if (navigation == null) PanelTokens.panelInset else PanelTokens.iconGap,
                    top = PanelTokens.panelInset,
                    bottom = PanelTokens.panelInset,
                ).heightIn(min = PanelTokens.touchTarget),
            contentAlignment = Alignment.CenterStart,
        ) { ProvideTextStyle(panelTextStyle()) { content() } }
        if (action != null) {
            AnimatedVisibility(actionShown, enter = fadeIn(motionTween()), exit = fadeOut(motionTween())) { action() }
        }
    }
}
