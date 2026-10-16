package com.otus.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The row being dragged and how far it has moved. The layout that owns it decides what a drop
 * means: [layout] gives the callback that gets the row's index and offset on release. The dragged
 * row can reach past either end of the column by [overhang] of its own height.
 */
internal class ReorderDrag(
    private val overhang: Float = 0f,
) {
    var key by mutableStateOf<Any?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set
    val heights = mutableStateMapOf<Any, Int>()
    private var keys: List<Any> = emptyList()
    private var spacing = 0f
    private var onDrop: (from: Int, offset: Float) -> Unit = { _, _ -> }

    private val from: Int
        get() = keys.indexOf(key)

    /** The measured height of each row, in order. */
    val rowHeights: List<Int>
        get() = keys.map { heights[it] ?: 0 }

    fun layout(
        keys: List<Any>,
        spacing: Float,
        onDrop: (from: Int, offset: Float) -> Unit,
    ) {
        if (keys != this.keys || spacing != this.spacing) cancel()
        heights.keys.filter { it !in keys }.forEach(heights::remove)
        this.keys = keys
        this.spacing = spacing
        this.onDrop = onDrop
    }

    fun start(key: Any) {
        if (key !in keys || rowHeights.any { it <= 0 }) return
        this.key = key
        offset = 0f
    }

    fun moveBy(delta: Float) {
        from.takeIf { it >= 0 }?.let { offset = clampDrag(rowHeights, it, offset + delta, spacing, overhang) }
    }

    fun drop() {
        val from = from
        val offset = offset
        cancel()
        if (from >= 0) onDrop(from, offset)
    }

    fun cancel() {
        key = null
        offset = 0f
    }
}
