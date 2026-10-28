package com.otus.designsystem.shared.reorder.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.shared.rows.elements.MAX_INDENT_STEPS
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.rows.ReorderTreeTokens

/**
 * Draws the drop line of a reorderable tree over its rows, [y] px from the top and indented to
 * [depth], or nothing while [y] is null. It does not change the layout.
 */
internal fun Modifier.dropLine(
    y: Float?,
    depth: Int,
): Modifier =
    drawWithContent {
        drawContent()
        if (y != null) drawDropLine(y, depth)
    }

/** A ring where a row at [depth] starts its icon, then a line with round ends to the row's end. */
private fun DrawScope.drawDropLine(
    y: Float,
    depth: Int,
) {
    val thickness = ReorderTreeTokens.lineThickness.toPx()
    val radius = ReorderTreeTokens.ringRadius.toPx()
    val start =
        (
            ReorderTreeTokens.lineStart + ReorderTreeTokens.indentStep *
                depth.coerceIn(
                    0,
                    MAX_INDENT_STEPS,
                )
        ).toPx()
    val end = size.width - ReorderTreeTokens.lineEnd.toPx()
    val reach = radius + thickness / 2
    val middle = y.coerceIn(reach, (size.height - reach).coerceAtLeast(reach))
    val color = OtusColors.accentLight

    fun at(x: Float) = Offset(if (layoutDirection == LayoutDirection.Rtl) size.width - x else x, middle)
    drawCircle(color, radius, at(start + radius), style = Stroke(thickness))
    drawLine(color, at(start + radius * 2), at(end), thickness, StrokeCap.Round)
}
