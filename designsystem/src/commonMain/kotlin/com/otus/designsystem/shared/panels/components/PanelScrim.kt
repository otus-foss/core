package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * The dim over a page while its [expansion] stands out, deepening as the panel grows to its first
 * stop. A tap on it brings the panel back. Place it between the page and its control panel.
 */
@Composable
public fun PanelScrim(
    expansion: PanelExpansion,
    modifier: Modifier = Modifier,
) {
    if (!expansion.expanded) return
    // Taps only: the scrim is not a control screen readers should stop on.
    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = expansion.position.coerceAtMost(1f) }
            .background(OtusColors.scrim.copy(alpha = SheetTokens.SCRIM_ALPHA))
            .pointerInput(expansion) { detectTapGestures { expansion.collapse() } },
    )
}
