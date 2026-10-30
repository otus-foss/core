package com.otus.designsystem.shared.panels.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens

internal fun Modifier.panelSurface(
    geometry: PanelLayoutState,
    overlap: () -> Float,
): Modifier =
    drawWithCache {
        val border = Stroke(PanelTokens.borderWidth.toPx())
        onDrawBehind {
            val frame = geometry.frame(overlap(), PanelTokens.cutoutGap.toPx(), border.width) ?: return@onDrawBehind
            drawPanelFrame(frame, border)
        }
    }

private fun DrawScope.drawPanelFrame(
    frame: PanelFrame,
    border: Stroke,
) {
    val viewport = Path().apply { addRect(Rect(frame.viewport.left, 0f, frame.viewport.right, size.height)) }
    // Holes are opaque geometry on their pills. Blending them by opacity would show two half-visible
    // contours while pill groups swap.
    val inset = border.width / 2
    val outline = Rect(inset, inset, size.width - inset, frame.bottom)
    val panel = Path().apply { addRoundRect(RoundRect(outline, CornerRadius(PanelTokens.panelRadius.toPx()))) }
    val surface =
        frame.holes.fold(panel) { shape, hole ->
            val rect = hole.bounds
            val cutout = Path().apply { addRoundRect(RoundRect(rect, CornerRadius(rect.height / 2))) }
            val clipped = if (hole.scrolling) Path.combine(PathOperation.Intersect, cutout, viewport) else cutout
            Path.combine(PathOperation.Difference, shape, clipped)
        }
    drawPath(surface, OtusColors.bgDark)
    drawPath(surface, OtusColors.strokeBgDark, style = border)
}
