package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.selection.FilterTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/** One group of [FilterPanel] choices: a small `text3` [label] above its chips, which wrap. */
@Composable
public fun FilterSection(
    label: String,
    modifier: Modifier = Modifier,
    chips: @Composable FlowRowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FilterTokens.labelGap)) {
        Text(
            label,
            Modifier.padding(start = FilterTokens.labelInset).semantics { heading() },
            style = panelTextStyle(),
            color = OtusColors.text3,
            fontSize = FilterTokens.labelSize,
        )
        // Each chip's 48 dp touch target already keeps rows 12 dp apart, so rows add no gap of their own.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipGap), content = chips)
    }
}
