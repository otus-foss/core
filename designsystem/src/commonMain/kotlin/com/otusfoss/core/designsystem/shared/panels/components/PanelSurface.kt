package com.otusfoss.core.designsystem.shared.panels.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.toPath
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens

internal fun Modifier.panelSurface(geometry: PanelLayoutState): Modifier =
    drawWithCache {
        val border = Stroke(Sizes.borderWidth.toPx())
        onDrawBehind {
            val frame = geometry.frame(PanelTokens.pillRing.toPx(), border.width) ?: return@onDrawBehind
            drawPanelFrame(frame, border)
        }
    }

private fun DrawScope.drawPanelFrame(
    frame: PanelFrame,
    border: Stroke,
) {
    // Rail holes reach into the trailing action's hole, so they merge with it like neighboring pills
    // do and the pills' fade is the only edge, never a cut where the rail stops.
    val railEnd =
        frame.holes
            .filterNot { it.scrolling }
            .minOfOrNull { it.bounds.center.x }
            ?.coerceAtLeast(frame.viewport.right)
            ?: frame.viewport.right
    val viewport = Path().apply { addRect(Rect(frame.viewport.left, 0f, railEnd, size.height)) }
    // Holes are opaque geometry on their pills. Blending them by opacity would show two half-visible
    // contours while pill groups swap.
    val surface =
        frame.holes.fold(panelShape(frame.bottom - border.width / 2)) { shape, hole ->
            val rect = hole.bounds
            val cutout = Path().apply { addRoundRect(RoundRect(rect, CornerRadius(rect.height / 2))) }
            val clipped = if (hole.scrolling) Path.combine(PathOperation.Intersect, cutout, viewport) else cutout
            Path.combine(PathOperation.Difference, shape, clipped)
        }
    drawPath(surface, OtusColors.bgDark)
    // The panel's top and sides run past the screen edges, so only the bottom edge and the holes show a stroke.
    clipRect(0f, 0f, size.width, size.height) { drawPath(surface, OtusColors.strokeBgDark, style = border) }
}

/** Square at the top and sides, which sit past the screen edges, and smoothly rounded at the [bottom] corners. */
private fun DrawScope.panelShape(bottom: Float): Path {
    val outside = Sizes.borderWidth.toPx()
    return PanelTokens.bottomShape.toPath(Rect(-outside, -outside, size.width + outside, bottom), layoutDirection, this)
}
