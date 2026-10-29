package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_remove
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.SidebarTokens

/** The minus that removes the tile [label] in a tile editor, with a full touch target around it. */
@Composable
internal fun TileRemoveBadge(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(PanelTokens.touchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Remove $label" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(SidebarTokens.removeBadge).background(OtusColors.bgLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
                Symbol(Res.drawable.symbol_remove, size = SidebarTokens.removeIcon)
            }
        }
    }
}

/**
 * The arc on a tile's bottom-end corner in a tile editor. Dragging it resizes the tile, which the
 * caller handles in [modifier]. It takes no taps, since its touch target covers much of a small
 * tile; accessibility services resize through the tile's actions.
 */
@Composable
internal fun TileResizeHandle(
    label: String,
    modifier: Modifier = Modifier,
) {
    val color = OtusColors.text2
    val mirrored = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(
        modifier
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = "Resize $label" },
    ) {
        val radius = SidebarTokens.resizeArc.toPx()
        val inset = SidebarTokens.resizeInset.toPx()
        // The bottom-end quarter of a circle, so it follows the tile's rounded corner.
        drawArc(
            color,
            startAngle = if (mirrored) 90f else 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft =
                Offset(
                    if (mirrored) inset else size.width - inset - radius * 2,
                    size.height - inset - radius * 2,
                ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(SidebarTokens.resizeStroke.toPx(), cap = StrokeCap.Round),
        )
    }
}
