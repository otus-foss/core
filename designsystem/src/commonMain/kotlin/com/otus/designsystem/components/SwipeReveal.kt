package com.otus.designsystem.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

/**
 * Replaces this path with the area a swiped row uncovers: a body the row's full height with a
 * rounded free end, joined to the row's edge through concave fillets that reach past the row's
 * top and bottom into its neighbors. [fromLeft] attaches it to the left edge; otherwise it is
 * mirrored onto the right.
 */
internal fun Path.swipeReveal(
    size: Size,
    width: Float,
    fromLeft: Boolean,
    radii: RevealRadii,
) {
    rewind()
    if (width <= 0f) return
    val (f, c) = radii
    val h = size.height
    val x = { distance: Float -> if (fromLeft) distance else size.width - distance }
    moveTo(x(0f), -f)
    // Concave fillet from the edge, above the row, into the body's top.
    cubicTo(x(0f), KAPPA * f - f, x(f - KAPPA * f), 0f, x(f), 0f)
    lineTo(x(width - c), 0f)
    cubicTo(x(width - c + KAPPA * c), 0f, x(width), c - KAPPA * c, x(width), c)
    lineTo(x(width), h - c)
    cubicTo(x(width), h - c + KAPPA * c, x(width - c + KAPPA * c), h, x(width - c), h)
    lineTo(x(f), h)
    // Concave fillet from the body's bottom back out to the edge, below the row.
    cubicTo(x(f - KAPPA * f), h, x(0f), h + f - KAPPA * f, x(0f), h + f)
    close()
}

/** Control-point distance, as a share of the radius, for a cubic quarter circle. */
private const val KAPPA = 0.5523f
