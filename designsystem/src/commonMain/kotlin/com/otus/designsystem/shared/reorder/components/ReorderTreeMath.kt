package com.otus.designsystem.shared.reorder.components

/**
 * Where the row dragged from [from] by [offset] lands in a tree of rows with [heights], judged by
 * the dragged row's center. The [span] rows after it, its nested rows, move with it as one block.
 * The middle half of a row means onto it; its upper quarter means the gap above it, and its lower
 * quarter the gap below it. Null over the block itself or the gaps on either side of it, where the
 * rows would stay put.
 */
internal fun treeDropTarget(
    heights: List<Int>,
    from: Int,
    offset: Float,
    span: Int = 0,
): TreeDropTarget? {
    val tops = rowTops(heights, spacing = 0f)
    val block = from..from + span
    val center = tops[from] + offset + heights[from] * HALF
    val over = heights.indices.firstOrNull { center < tops[it] + heights[it] } ?: heights.lastIndex
    val share = (center - tops[over]) / heights[over]
    return when {
        over in block -> null
        share < QUARTER -> TreeDropTarget.Before(over)
        share > 1 - QUARTER -> TreeDropTarget.Before(over + 1)
        else -> TreeDropTarget.Into(over)
    }?.takeUnless { it is TreeDropTarget.Before && it.index in block.first..block.last + 1 }
}

/** How many rows after [from] are nested inside it: the rows deeper than it, up to the next that is not. */
internal fun nestedSpan(
    depths: List<Int>,
    from: Int,
): Int =
    depths
        .drop(from + 1)
        .takeWhile { it > depths[from] }
        .size

/**
 * How far down the tree the line for [target] sits: at the top of the row below the gap, or at the
 * bottom of the row it goes into.
 */
internal fun dropLineY(
    heights: List<Int>,
    target: TreeDropTarget,
): Float {
    val tops = rowTops(heights, spacing = 0f)
    return when (target) {
        is TreeDropTarget.Before -> tops.getOrElse(target.index) { tops.last() + heights.last() }
        is TreeDropTarget.Into -> tops[target.index] + heights[target.index]
    }
}

/**
 * The nesting depth the line for [target] shows, given each row's [depth]: the row below the gap,
 * the last row after the end, or one deeper than the row it goes into.
 */
internal fun dropLineDepth(
    depths: List<Int>,
    target: TreeDropTarget,
): Int =
    when (target) {
        is TreeDropTarget.Before -> depths[target.index.coerceAtMost(depths.lastIndex)]
        is TreeDropTarget.Into -> depths[target.index] + 1
    }

private const val QUARTER = 0.25f
