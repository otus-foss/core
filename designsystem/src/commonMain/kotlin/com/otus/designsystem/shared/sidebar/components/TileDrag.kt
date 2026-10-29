package com.otus.designsystem.shared.sidebar.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.toOffset

/**
 * The tile being dragged in a [SidebarTileEditor], where it is, and the order it would drop into.
 * While a tile is dragged, the others take their places in that order. After a drop, the editor
 * keeps showing the dropped order until the caller's keys change, so the tiles do not jump back
 * for a frame.
 */
internal class TileDrag {
    var key by mutableStateOf<Any?>(null)
        private set

    /** The dragged tile's top-start corner. */
    var position by mutableStateOf(Offset.Zero)
        private set
    private var order by mutableStateOf<List<Any>?>(null)
    private var dropped by mutableStateOf<List<Any>?>(null)
    private var keys: List<Any> = emptyList()

    /** The order to show the tiles in: the drag's, the dropped one, or [keys]. */
    fun shown(keys: List<Any>): List<Any> = order ?: dropped?.takeIf { it.toSet() == keys.toSet() } ?: keys

    /** A change to the caller's [keys] ends a drag and replaces a dropped order. */
    fun sync(keys: List<Any>) {
        if (keys == this.keys) return
        this.keys = keys
        dropped = null
        cancel()
    }

    fun start(
        key: Any,
        at: IntOffset,
    ) {
        if (key !in keys) return
        this.key = key
        position = at.toOffset()
        order = keys
    }

    /**
     * Moves the tile by [delta] and puts it where its center lands nearest a slot. [place] gives the
     * bounds of each tile in an order. Returns whether the order changed.
     */
    fun moveBy(
        delta: Offset,
        place: (List<Any>) -> List<IntRect>,
    ): Boolean {
        val key = key
        val current = order
        if (key == null || current == null) return false
        position += delta
        val size = place(current)[current.indexOf(key)].size
        val center = position + Offset(size.width / 2f, size.height / 2f)
        val others = current.filter { it != key }
        val best =
            (0..others.size)
                .map { index -> others.take(index) + key + others.drop(index) }
                .minBy { candidate ->
                    (place(candidate)[candidate.indexOf(key)].center.toOffset() - center).getDistanceSquared()
                }
        order = best
        return best != current
    }

    /** Ends the drag and returns the tile's old and new index, or null when it stayed. */
    fun drop(): Pair<Int, Int>? {
        val key = key ?: return null
        val from = keys.indexOf(key)
        val to = order?.indexOf(key) ?: from
        dropped = order
        cancel()
        return (from to to).takeIf { from != to }
    }

    fun cancel() {
        key = null
        order = null
    }
}
