package com.otus.designsystem.mail.read.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.tokens.rows.StripTokens
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A [PositionStrip] whose pages turn under a finger, as a music player's mini player turns its
 * tracks: the card and its chevron stay, while each page's avatar, title, dots, and detail slide
 * inside it with [position], such as 1.4 while a drag goes from the second page toward the third.
 * The next page's content slides in beside the current one. [page] gives each page's content, and
 * [trailing] shows on every page. Screen readers hear the page at [current].
 */
@Composable
public fun SlidingPositionStrip(
    count: Int,
    current: Int,
    position: () -> Float,
    page: (Int) -> PositionPage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    // Screen readers hear a page that exists, even if a caller's index runs past the ends.
    val heard = current.coerceIn(0, (count - 1).coerceAtLeast(0))
    val shown = page(heard)
    val spoken = shown.description ?: StripPosition(count, heard, shown.detail).spoken(shown.heading)
    PositionCard(spoken, onClick, modifier) {
        PageTrack(count, position, Modifier.weight(1f)) { index ->
            val content = page(index)
            val place = StripPosition(count, index, content.detail)
            PositionInfo(content.heading, place, Modifier.fillMaxWidth(), trailing)
        }
    }
}

/**
 * [count] pages side by side, each as wide as the track and [StripTokens.start] apart, moved
 * by [position] and cut off at the track's edges. Only the page at or before [position] and the
 * one after it are composed.
 */
@Composable
private fun PageTrack(
    count: Int,
    position: () -> Float,
    modifier: Modifier,
    content: @Composable (Int) -> Unit,
) {
    // Mirrored layouts place the next page on the left, as a pager does.
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val inView by remember(count) {
        derivedStateOf { floor(position()).toInt().let { listOf(it, it + 1) }.filter { it in 0 until count } }
    }
    val gap = with(LocalDensity.current) { StripTokens.start.roundToPx() }
    BoxWithConstraints(modifier.clipToBounds(), contentAlignment = Alignment.CenterStart) {
        val step = constraints.maxWidth + gap
        inView.forEach { index ->
            key(index) {
                Box(Modifier.offset { IntOffset(((index - position()) * step * direction).roundToInt(), 0) }) {
                    content(index)
                }
            }
        }
    }
}
