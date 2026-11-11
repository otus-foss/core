package com.otusfoss.core.designsystem.shared.reorder.components

/** The top of each row with [heights], laid out [spacing] apart. */
internal fun rowTops(
    heights: List<Int>,
    spacing: Float,
): List<Float> = heights.runningFold(0f) { top, height -> top + height + spacing }.dropLast(1)

/**
 * [offset] limited so the row dragged from [from] stays within the column, or reaches past either
 * end by [overhang] of its own height.
 */
internal fun clampDrag(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
    overhang: Float = 0f,
): Float {
    val tops = rowTops(heights, spacing)
    val end = tops.last() + heights.last()
    val slack = heights[from] * overhang
    return offset.coerceIn(-tops[from] - slack, end - tops[from] - heights[from] + slack)
}

/**
 * Where the row dragged from [from] by [offset] lands. It passes a row below once its bottom edge
 * crosses that row's middle, and a row above once its top edge does, so it can reach either end
 * of the column whatever the rows' heights.
 */
internal fun reorderTarget(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
): Int {
    val tops = rowTops(heights, spacing)
    val top = tops[from] + offset
    val bottom = top + heights[from]
    val passedBelow = (from + 1..heights.lastIndex).count { bottom > tops[it] + heights[it] * HALF }
    val passedAbove = (0 until from).count { top < tops[it] + heights[it] * HALF }
    return from + passedBelow - passedAbove
}

internal const val HALF: Float = 0.5f

/** How far the row at [index] steps aside while the row dragged from [from] hovers over [target]. */
internal fun reorderShift(
    index: Int,
    from: Int,
    target: Int,
    draggedHeight: Int,
    spacing: Float,
): Float =
    when (index) {
        in from + 1..target -> -(draggedHeight + spacing)
        in target until from -> draggedHeight + spacing
        else -> 0f
    }
