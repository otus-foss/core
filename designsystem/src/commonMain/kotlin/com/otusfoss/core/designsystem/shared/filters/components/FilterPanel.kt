package com.otusfoss.core.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.panels.components.PanelSheet
import com.otusfoss.core.designsystem.shared.panels.components.PanelSheetHeader
import com.otusfoss.core.designsystem.shared.panels.elements.PanelNavigation
import com.otusfoss.core.designsystem.shared.panels.elements.PanelTitle
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens

/**
 * Filters in the control panel, unfolded as a [PanelSheet] [collapsedHeight] tall at rest. The
 * [header], usually a [FilterPanelTitle], stays in the panel's header row. The [body] scrolls under
 * the [footer], which floats at the bottom, and fades out over [Sizes.edgeFade] just above
 * it. Back, a tap on the scrim, and an upward swipe call [onDismiss]. The panel takes all of its
 * room even when the body is short, so pages of different lengths keep one height and leave space
 * above the footer.
 */
@Composable
public fun FilterPanel(
    open: Boolean,
    collapsedHeight: Dp,
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
) {
    PanelSheet(open, collapsedHeight, onDismiss, header, modifier) {
        FloatingFooter(footer, Modifier.weight(1f), body)
    }
}

/**
 * A [FilterPanel]'s header row: the chevron, which runs [onClose], the [title], which screen
 * readers announce as the pane's, and [actions] at the end, such as [FilterResetAction].
 */
@Composable
public fun FilterPanelTitle(
    title: String,
    onClose: () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    PanelSheetHeader(
        navigation = { PanelNavigation("Close filters", onClose) },
        modifier = Modifier.semantics { paneTitle = title },
        actions = actions,
    ) { PanelTitle(title, Modifier.padding(start = PanelTokens.titleStart)) }
}

/** The [body], which scrolls under the [footer] floating at the bottom and fades out above it. */
@Composable
private fun FloatingFooter(
    footer: @Composable () -> Unit,
    modifier: Modifier,
    body: @Composable () -> Unit,
) {
    var footerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(modifier) {
        Column(Modifier.fillMaxSize().fadeBottom(footerHeight).verticalScroll(rememberScrollState())) {
            body()
            Spacer(Modifier.height(footerHeight))
        }
        Box(
            Modifier.align(Alignment.BottomCenter).onSizeChanged {
                footerHeight = with(density) { it.height.toDp() }
            },
        ) { footer() }
    }
}

/**
 * Hides the content behind the last [height], where the footer floats, and fades it out over
 * [Sizes.edgeFade] above that, as the pill rail fades its pills before its edge.
 */
private fun Modifier.fadeBottom(height: Dp): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val end = size.height - height.toPx()
            val start = end - Sizes.edgeFade.toPx()
            // The gradient clamps to transparent below its end, so the footer's band stays clear.
            drawRect(
                Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = start, endY = end),
                topLeft = Offset(0f, start),
                blendMode = BlendMode.DstIn,
            )
        }
