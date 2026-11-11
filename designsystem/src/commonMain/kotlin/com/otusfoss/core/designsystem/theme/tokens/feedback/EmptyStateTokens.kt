package com.otusfoss.core.designsystem.theme.tokens.feedback

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/** An empty state: its inset from the page edge, its symbol, the gap between its parts, and its title. */
internal object EmptyStateTokens {
    public val inset: Dp = Spacing.gutter
    public val iconSize: Dp = 64.dp
    public val itemGap: Dp = Spacing.l
    public val titleSize: TextUnit = TypeScale.title
}
