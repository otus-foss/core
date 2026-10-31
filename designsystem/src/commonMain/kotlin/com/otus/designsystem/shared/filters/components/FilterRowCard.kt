package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.selection.FilterTokens

/**
 * [rows] of a filter page on one `bgRegular` card with a `strokeBgDark` border, a `strokeBgDark`
 * line between each two, such as [FilterValueRow]s or the From and To [PeopleLine]s.
 */
@Composable
public fun FilterRowCard(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    val shape = SmoothCornerShape(FilterTokens.cardRadius)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OtusColors.bgRegular)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape),
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) CardDivider()
            row()
        }
    }
}

@Composable
private fun CardDivider() {
    Box(Modifier.fillMaxWidth().height(PanelTokens.borderWidth).background(OtusColors.strokeBgDark))
}
