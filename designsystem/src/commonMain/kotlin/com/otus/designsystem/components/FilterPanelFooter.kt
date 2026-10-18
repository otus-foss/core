package com.otus.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusAlpha
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A [FilterPanel]'s fixed bottom row: a Reset pill, dimmed while [resetEnabled] is false, and the
 * primary [applyLabel] button filling the rest. While [counting], a small progress ring follows
 * the label. Screen readers announce a changed label politely.
 */
@Composable
public fun FilterPanelFooter(
    applyLabel: String,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    applyEnabled: Boolean = true,
    resetEnabled: Boolean = true,
    counting: Boolean = false,
    resetLabel: String = "Reset",
) {
    Row(
        modifier.fillMaxWidth().padding(FilterTokens.footerPadding),
        horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipGap),
    ) {
        FooterButton(resetLabel, onReset, resetEnabled, primary = false)
        FooterButton(applyLabel, onApply, applyEnabled, primary = true, Modifier.weight(1f), counting)
    }
}

@Composable
private fun FooterButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    primary: Boolean,
    modifier: Modifier = Modifier,
    counting: Boolean = false,
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        modifier
            .height(FilterTokens.footerButtonHeight)
            .alpha(if (enabled) 1f else OtusAlpha.DIMMED)
            .clickable(interactions, null, enabled, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions),
        shape = CircleShape,
        color = if (primary) OtusColors.accentDark else OtusColors.bgRegular,
        contentColor = OtusColors.text1,
        border = BorderStroke(PanelTokens.borderWidth, if (primary) OtusColors.accentRegular else OtusColors.bgLight),
    ) {
        Box(Modifier.padding(horizontal = SheetTokens.inset), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    Modifier.semantics { if (primary) liveRegion = LiveRegionMode.Polite },
                    style = panelTextStyle(),
                    fontSize = FilterTokens.footerTextSize,
                    fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
                if (counting) {
                    CircularProgressIndicator(
                        Modifier.size(FilterTokens.progressSize),
                        color = OtusColors.text1,
                        strokeWidth = FilterTokens.progressStroke,
                    )
                }
            }
        }
    }
}
