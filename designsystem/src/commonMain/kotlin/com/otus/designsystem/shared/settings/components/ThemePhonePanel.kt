package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * The phone's own control panel, drawn in [palette] over the page's panel as a
 * [ThemePhonePreview] grows: it fades in with the [expansion], so the page's panel turns into the
 * phone's. Give it the page panel's size. A tap or drag on it shrinks the phone like one on the phone.
 */
@Composable
public fun ThemePhonePanel(
    palette: OtusPalette,
    expansion: PhoneExpansion,
    modifier: Modifier = Modifier,
) {
    // Left out while shrunk, so it takes no taps meant for the page's panel.
    if (expansion.progress.value == 0f) return
    PhoneColors(palette) {
        Box(
            modifier
                .graphicsLayer { alpha = expansion.progress.value }
                .clearAndSetSemantics {}
                .dragsPhone(expansion)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = expansion::collapse)
                .phoneSides(palette)
                .background(palette.bgDark, PanelTokens.bottomShape)
                .edge(PanelTokens.bottomShape, palette.strokeBgDark, sides = false),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(ThemeTokens.previewPadding)
                    .height(ThemeTokens.previewBarHeight)
                    .background(palette.bgRegular, CircleShape)
                    .padding(horizontal = ThemeTokens.previewBarPadding),
                horizontalArrangement = Arrangement.spacedBy(ThemeTokens.previewIconGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Symbol(Res.drawable.symbol_search, size = ThemeTokens.previewIcon)
                Text(
                    "Search inbox",
                    Modifier.weight(1f),
                    color = palette.text2,
                    fontSize = ThemeTokens.previewSubjectSize,
                )
                Box(Modifier.size(ThemeTokens.previewIcon).background(palette.accentRegular, CircleShape))
            }
        }
    }
}
