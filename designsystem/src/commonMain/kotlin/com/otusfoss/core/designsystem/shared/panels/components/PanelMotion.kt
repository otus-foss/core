package com.otusfoss.core.designsystem.shared.panels.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How a [ContextPanel] moves: [collapse] reads a [PanelCollapse.progress] as a list scrolls it
 * away, and an [expansion] lets the user drag it taller, showing [expanded] in the room it grows
 * below its header row. Without an expansion the panel keeps its height. Uncollapsed, the panel
 * is at least [minHeight] tall, so rows below keep their place while its pills are away.
 */
public class PanelMotion(
    public val collapse: () -> Float = { 0f },
    public val expansion: PanelExpansion? = null,
    public val minHeight: Dp = 0.dp,
    public val expanded: @Composable () -> Unit = {},
)
