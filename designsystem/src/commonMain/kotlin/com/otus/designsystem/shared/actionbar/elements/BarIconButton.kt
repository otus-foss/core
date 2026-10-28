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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
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
    BasicTooltipBox(
        positionProvider = AboveAnchor,
        tooltip = { BarTooltip(label) },
        state = rememberBasicTooltipState(),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .size(PanelTokens.touchTarget)
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
            .background(OtusColors.bgLight, RoundedCornerShape(PanelTokens.panelRadius))
            .padding(horizontal = PanelTokens.chipPadding, vertical = PanelTokens.iconGap),
        fontSize = PageTokens.captionSize,
    )
}

/** Centers the tooltip above its anchor, inside the window, with a small gap. */
private object AboveAnchor : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.center.x - popupContentSize.width / 2
        val y = anchorBounds.top - popupContentSize.height
        return IntOffset(
            x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y.coerceAtLeast(0),
        )
    }
}
