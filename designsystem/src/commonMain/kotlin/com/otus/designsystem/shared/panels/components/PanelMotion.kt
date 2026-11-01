package com.otus.designsystem.shared.panels.components

import androidx.compose.runtime.Composable

/**
 * How a [ContextPanel] moves: [collapse] reads a [PanelCollapse.progress] as a list scrolls it
 * away, and an [expansion] lets the user drag it taller, showing [expanded] in the room it grows
 * below its header row. Without an expansion the panel keeps its height.
 */
public class PanelMotion(
    public val collapse: () -> Float = { 0f },
    public val expansion: PanelExpansion? = null,
    public val expanded: @Composable () -> Unit = {},
)
