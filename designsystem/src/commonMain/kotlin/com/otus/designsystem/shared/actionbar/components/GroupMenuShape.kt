package com.otus.designsystem.shared.actionbar.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.shapes.smoothCorner

/**
 * A bar's group grown into the menu above it: one surface whose panel spans the shape's width,
 * and whose lower part is the group, [group] pixels tall. [cut] is the empty space beside the
 * group, under the panel, measured from the shape's bottom end corner. Where the panel meets the
 * group, the edge curves in by [fillet], [radius] plus the bar's gap once the menu is open, so the
 * curve shares its center with the primary's corner; the other corners round by [radius]. Measuring the cut
 * from the end keeps it in place when a border draws the shape slightly smaller.
 */
internal class GroupMenuShape(
    private val cut: Size,
    private val group: Float,
    private val radius: Float,
    private val fillet: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height
        val ltr = layoutDirection == LayoutDirection.Ltr
        val groupEnd = width - cut.width
        val panelBottom = height - cut.height
        val cuts = cut.width > 0f && cut.height > 0f && panelBottom > 0f

        fun at(
            x: Float,
            y: Float,
        ) = Offset(if (ltr) x else width - x, y)
        val end = Offset(if (ltr) 1f else -1f, 0f)
        val down = Offset(0f, 1f)
        return Outline.Generic(
            Path().apply {
                // Each corner may take half of each edge it touches.
                smoothCorner(at(width, 0f), end, down, radius, minOf(width, if (cuts) panelBottom else height) / 2)
                if (cuts) {
                    // Plain arcs, with the curve first in line for the edge it shares, so the open
                    // menu keeps an even gap around the round corner of what sits beside the group.
                    val curve = minOf(fillet, cut.height - group / 2, cut.width * fillet / (fillet + radius))
                    val corner = minOf(radius, panelBottom / 2, cut.width - curve)
                    smoothCorner(at(width, panelBottom), down, -end, corner, corner)
                    smoothCorner(at(groupEnd, panelBottom), -end, down, curve, curve)
                }
                smoothCorner(at(groupEnd, height), down, -end, radius, minOf(groupEnd, group, cut.height) / 2)
                smoothCorner(at(0f, height), -end, -down, radius, minOf(groupEnd, group) / 2)
                smoothCorner(at(0f, 0f), -down, end, radius, minOf(width, height) / 2)
                close()
            },
        )
    }
}
