package com.otusfoss.core.designsystem.shared.actionbar.elements

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.overlays.elements.AbovePopup
import com.otusfoss.core.designsystem.shared.overlays.elements.PopupAlign
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.MenuTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * An icon-only action in a 48 dp target. Long-pressing it shows [label] above it, because the
 * icon has no text of its own; disabled, its icon turns text3. A toggle passes [on]: while it is
 * true, a round `selectedFill` sits behind the icon and accessibility services read it as
 * selected. Null means the action is no toggle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun BarIconButton(
    label: String,
    symbol: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    on: Boolean? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val edge = with(LocalDensity.current) { MenuTokens.edge.roundToPx() }
    val fill by animateColorAsState(
        if (on == true) OtusColors.selectedFill else Color.Transparent,
        motionTween(MotionSpeed.Quick),
        label = "Bar toggle fill",
    )
    BasicTooltipBox(
        positionProvider = remember(edge) { AbovePopup(gap = 0, edge, PopupAlign.Center) },
        tooltip = { BarTooltip(label) },
        state = rememberBasicTooltipState(),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .size(Sizes.touchTarget)
                .semantics {
                    contentDescription = label
                    if (on != null) selected = on
                }.clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
                .controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(ActionBarTokens.onFillSize)
                    .background(fill, SmoothCornerShape(ActionBarTokens.onFillSize / 2)),
            )
            CompositionLocalProvider(LocalContentColor provides if (enabled) OtusColors.text1 else OtusColors.text3) {
                Symbol(symbol, size = ActionBarTokens.symbolSize)
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
