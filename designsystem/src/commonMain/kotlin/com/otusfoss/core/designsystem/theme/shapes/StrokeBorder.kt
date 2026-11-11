package com.otusfoss.core.designsystem.theme.shapes

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * A [width] border in [color] just inside [shape], drawn over the content like
 * [border][androidx.compose.foundation.border]. That one draws a path outline, such as a
 * [SmoothCornerShape]'s, into a bitmap that lands on whole pixels, so on a fractional position it
 * drifts from the fill and its top edge looks cut off. This one strokes the path directly.
 */
public fun Modifier.strokeBorder(
    width: Dp,
    color: Color,
    shape: Shape,
): Modifier =
    drawWithCache {
        val stroke = Stroke(width.toPx())
        val inset = stroke.width / 2
        val path =
            shape
                .shrunkBy(inset, size, this)
                .toPath(Rect(inset, inset, size.width - inset, size.height - inset), layoutDirection, this)
        onDrawWithContent {
            drawContent()
            drawPath(path, color, style = stroke)
        }
    }

/** The shape with each corner [inset] smaller, so a line [inset] inside it stays parallel to the edge. */
private fun Shape.shrunkBy(
    inset: Float,
    size: Size,
    density: Density,
): Shape {
    if (this !is CornerBasedShape) return this

    fun CornerSize.shrunk() = CornerSize((toPx(size, density) - inset).coerceAtLeast(0f))
    return copy(topStart.shrunk(), topEnd.shrunk(), bottomEnd.shrunk(), bottomStart.shrunk())
}
