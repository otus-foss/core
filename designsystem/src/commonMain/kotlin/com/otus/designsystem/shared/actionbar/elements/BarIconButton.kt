package com.otus.designsystem.shared.actionbar.elements

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.overlays.elements.AbovePopup
import com.otus.designsystem.shared.overlays.elements.PopupAlign
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.controls.ControlTokens
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * An icon-only action in a 48 dp target. Long-pressing it shows [label] above it, because the
 * icon has no text of its own; disabled, its icon turns text3.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun BarIconButton(
    label: String,
    symbol: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    mirrorInRtl: Boolean = false,
) {
    val interactions = remember { MutableInteractionSource() }
    val edge = with(LocalDensity.current) { MenuTokens.edge.roundToPx() }
    BasicTooltipBox(
        positionProvider = remember(edge) { AbovePopup(gap = 0, edge, PopupAlign.Center) },
        tooltip = { BarTooltip(label) },
        state = rememberBasicTooltipState(),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .size(Sizes.touchTarget)
                .semantics { contentDescription = label }
                .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
                .controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides if (enabled) OtusColors.text1 else OtusColors.text3) {
                Symbol(symbol, mirrorInRtl, ActionBarTokens.symbolSize)
            }
        }
    }
}

@Composable
private fun BarTooltip(label: String) {
    Text(
        label,
        Modifier
            .background(OtusColors.bgLight, SmoothCornerShape(PanelTokens.panelRadius))
            .padding(horizontal = ControlTokens.chipPadding, vertical = Spacing.iconGap),
        fontSize = PageTokens.captionSize,
    )
}
