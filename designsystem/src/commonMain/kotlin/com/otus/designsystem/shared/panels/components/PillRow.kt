package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/**
 * Pills in one row that scrolls sideways, fading at an edge with more pills past it, as the control
 * panel's rail does, with a [trailing] button that stays at the end, such as a filter button.
 */
@Composable
public fun PillRow(
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    pills: @Composable RowScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier.fillMaxWidth().padding(end = PanelTokens.panelInset),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).scrollFade(scroll)) {
            Row(
                Modifier
                    .horizontalScroll(scroll, overscrollEffect = null)
                    .padding(start = PanelTokens.panelInset),
                horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
                verticalAlignment = Alignment.CenterVertically,
                content = pills,
            )
        }
        trailing?.invoke()
    }
}

/** Fades the content at an edge [scroll] has more past, as the control panel's rail does. */
internal fun Modifier.scrollFade(scroll: ScrollState): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fade = PanelTokens.fadeWidth.toPx().coerceAtMost(size.width / 2)
            drawEdgeFade((scroll.value / fade).coerceIn(0f, 1f), fade, atEnd = false)
            drawEdgeFade(((scroll.maxValue - scroll.value) / fade).coerceIn(0f, 1f), fade, atEnd = true)
        }
