package com.otus.designsystem.shared.overlays.elements

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Which edge of its anchor a popup lines up with, or its center. Start and end follow the layout direction. */
internal enum class PopupAlign { Start, Center, End }

/**
 * Puts a popup [gap] pixels above its anchor, lined up by [align], and below it when there is no
 * room above; it stays [edge] pixels inside the window. With a [point], the anchor is that point
 * inside the element the popup is called from, such as where the user long-pressed.
 */
internal class AbovePopup(
    private val gap: Int,
    private val edge: Int,
    private val align: PopupAlign,
    private val point: IntOffset? = null,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchor = point?.let { IntRect(anchorBounds.topLeft + it, IntSize.Zero) } ?: anchorBounds
        val above = anchor.top - gap - popupContentSize.height
        val y = if (above >= edge) above else anchor.bottom + gap
        val x = anchor.popupStart(align, popupContentSize.width, layoutDirection)
        return IntOffset(
            x.keptInside(popupContentSize.width, windowSize.width, edge),
            y.keptInside(popupContentSize.height, windowSize.height, edge),
        )
    }
}

/** Puts a popup's corner on [point], flipping it to stay [edge] pixels inside the window. */
internal class AtPoint(
    private val point: IntOffset,
    private val edge: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset =
        IntOffset(
            flipToFit(anchorBounds.left + point.x, popupContentSize.width, windowSize.width),
            flipToFit(anchorBounds.top + point.y, popupContentSize.height, windowSize.height),
        )

    /** Starts the popup at [at], or ends it there when its [size] would not fit before the window's end. */
    private fun flipToFit(
        at: Int,
        size: Int,
        window: Int,
    ): Int = (if (at + size <= window - edge) at else at - size).keptInside(size, window, edge)
}

/** The left edge of a popup [width] pixels wide, lined up with this anchor by [align]. */
internal fun IntRect.popupStart(
    align: PopupAlign,
    width: Int,
    layoutDirection: LayoutDirection,
): Int {
    val ltr = layoutDirection == LayoutDirection.Ltr
    return when (align) {
        PopupAlign.Center -> center.x - width / 2
        PopupAlign.Start -> if (ltr) left else right - width
        PopupAlign.End -> if (ltr) right - width else left
    }
}

/**
 * This start of a popup [size] pixels long, moved to stay [edge] pixels inside a [window] that long,
 * or to [edge] when the popup is too long for that.
 */
internal fun Int.keptInside(
    size: Int,
    window: Int,
    edge: Int,
): Int = coerceIn(edge, maxOf(edge, window - size - edge))
