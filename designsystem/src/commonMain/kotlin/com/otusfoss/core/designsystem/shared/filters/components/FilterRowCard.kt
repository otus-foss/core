package com.otusfoss.core.designsystem.shared.filters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.selection.FilterTokens

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
            .strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, shape),
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) CardDivider()
            row()
        }
    }
}

@Composable
private fun CardDivider() {
    Box(Modifier.fillMaxWidth().height(Sizes.borderWidth).background(OtusColors.strokeBgDark))
}
