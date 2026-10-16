package com.otus.designsystem.components

/** The top of each row with [heights], laid out [spacing] apart. */
internal fun rowTops(
    heights: List<Int>,
    spacing: Float,
): List<Float> = heights.runningFold(0f) { top, height -> top + height + spacing }.dropLast(1)

/** [offset] limited so the row dragged from [from] stays within the column. */
internal fun clampDrag(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
): Float {
    val tops = rowTops(heights, spacing)
    val end = tops.last() + heights.last()
    return offset.coerceIn(-tops[from], end - tops[from] - heights[from])
}

/**
 * Where the row dragged from [from] by [offset] lands. It passes a row below once its bottom edge
 * crosses [pass] of that row's height, the middle by default, and a row above once its top edge
 * crosses the same share from that row's bottom, so it can reach either end of the column whatever
 * the rows' heights.
 */
internal fun reorderTarget(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
    pass: Float = HALF,
): Int {
    val tops = rowTops(heights, spacing)
    val top = tops[from] + offset
    val bottom = top + heights[from]
    val passedBelow = (from + 1..heights.lastIndex).count { bottom > tops[it] + heights[it] * pass }
    val passedAbove = (0 until from).count { top < tops[it] + heights[it] * (1 - pass) }
    return from + passedBelow - passedAbove
}

/**
 * The row that the row dragged from [from] by [offset] drops onto, or null. Its leading edge, the
 * bottom edge moving down or the top edge moving up, must lie in the middle half of that row; near
 * a row's edges the drag reorders instead.
 */
internal fun nestTarget(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
): Int? {
    val tops = rowTops(heights, spacing)
    val top = tops[from] + offset
    val bottom = top + heights[from]

    fun covers(
        row: Int,
        edge: Float,
    ): Boolean = (edge - tops[row]) / heights[row] in NEST_EDGE..1 - NEST_EDGE
    return heights.indices.firstOrNull { (it > from && covers(it, bottom)) || (it < from && covers(it, top)) }
}

/** Where a drag would drop: before the row at [to], or onto the row at [onto] when that is set. */
internal data class ReorderDrop(
    val to: Int,
    val onto: Int? = null,
)

/**
 * The drop for the row dragged from [from] by [offset]. With [nesting], a row passes another only
 * beyond that row's middle half, which [nestTarget] keeps for dropping onto it.
 */
internal fun reorderDrop(
    heights: List<Int>,
    from: Int,
    offset: Float,
    spacing: Float,
    nesting: Boolean,
): ReorderDrop =
    if (nesting) {
        ReorderDrop(
            reorderTarget(heights, from, offset, spacing, pass = 1 - NEST_EDGE),
            nestTarget(heights, from, offset, spacing),
        )
    } else {
        ReorderDrop(reorderTarget(heights, from, offset, spacing))
    }

/** The share of a row's height at each edge where a drag reorders rather than drops onto it. */
internal const val NEST_EDGE: Float = 0.25f
private const val HALF = 0.5f

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
