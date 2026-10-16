package com.otus.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class ReorderDrag {
    var key by mutableStateOf<Any?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set
    val heights = mutableStateMapOf<Any, Int>()
    private var keys: List<Any> = emptyList()
    private var spacing = 0f
    private var onMove: (Int, Int) -> Unit = { _, _ -> }
    private var onNest: ((Int, Int) -> Unit)? = null

    private val from: Int
        get() = keys.indexOf(key)

    private val rowHeights: List<Int>
        get() = keys.map { heights[it] ?: 0 }

    fun layout(
        keys: List<Any>,
        spacing: Float,
        onMove: (Int, Int) -> Unit,
        onNest: ((Int, Int) -> Unit)? = null,
    ) {
        if (keys != this.keys || spacing != this.spacing) cancel()
        heights.keys.filter { it !in keys }.forEach(heights::remove)
        this.keys = keys
        this.spacing = spacing
        this.onMove = onMove
        this.onNest = onNest
    }

    fun start(key: Any) {
        if (key !in keys || rowHeights.any { it <= 0 }) return
        this.key = key
        offset = 0f
    }

    fun moveBy(delta: Float) {
        from.takeIf { it >= 0 }?.let { offset = clampDrag(rowHeights, it, offset + delta, spacing) }
    }

    fun drop() {
        val from = from
        val onNest = onNest
        val drop = if (from >= 0) reorderDrop(rowHeights, from, offset, spacing, nesting = onNest != null) else null
        cancel()
        when {
            drop == null -> Unit
            drop.onto != null -> onNest?.invoke(from, drop.onto)
            drop.to != from -> onMove(from, drop.to)
        }
    }

    fun cancel() {
        key = null
        offset = 0f
    }
}
