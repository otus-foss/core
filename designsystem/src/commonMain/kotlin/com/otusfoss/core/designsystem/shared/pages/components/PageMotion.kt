package com.otusfoss.core.designsystem.shared.pages.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.abs

/** Chrome that every page draws in the same place, so a slide between two pages that both have it leaves it still. */
internal enum class PageChrome { ContextPanel, ActionBar }

/**
 * One page of a [SlidingPages] or [SlidingOverlay]. [fraction] is how far it has slid, in widths
 * toward the end edge; [partner] is the page it slides against, while a slide runs; and [leaving]
 * holds while it slides away. A slider nested in a page has that page's motion as its [parent],
 * and shares its [host].
 */
internal class PageMotion(
    private val parent: PageMotion?,
    /** Draws the chrome of every page in the outermost slider while it stays still. */
    val host: PinnedChromeHost,
    private val sign: Float,
    private val fraction: () -> Float,
    private val leaving: () -> Boolean,
    private val partner: () -> PageMotion?,
) {
    var width: Int by mutableIntStateOf(0)
    private val chrome = mutableStateListOf<PageChrome>()

    val offset: Float
        get() = fraction() * width * sign

    /** Every slider this page sits in, from its own outward. */
    private val chain: Sequence<PageMotion>
        get() = generateSequence(this) { it.parent }

    fun register(kind: PageChrome) = chain.forEach { it.chrome += kind }

    fun unregister(kind: PageChrome) = chain.forEach { it.chrome -= kind }

    private fun pins(kind: PageChrome): Boolean = partner()?.chrome?.contains(kind) == true

    /** Whether every slide this page is in has come to rest. */
    val still: Boolean
        get() = chain.all { it.fraction() == 0f }

    /** Whether any slide this page is in has [kind] on both pages. */
    fun pinned(kind: PageChrome): Boolean = chain.any { it.pins(kind) }

    /** Whether this page's copy of [kind] is the one leaving, under the arriving page's copy. */
    fun leaves(kind: PageChrome): Boolean = chain.any { it.pins(kind) && it.leaving() }

    /** Undoes each slide that both pages draw [kind] for, so the chrome stays where it is. */
    fun pinnedShift(kind: PageChrome): Float = chain.filter { it.pins(kind) }.sumOf { -it.offset.toDouble() }.toFloat()

    /** The leaving page's copy fades as it goes, under the arriving page's copy. */
    fun pinnedAlpha(kind: PageChrome): Float =
        chain
            .filter { it.pins(kind) && it.leaving() }
            .fold(1f) { alpha, motion -> alpha * (1f - abs(motion.fraction())).coerceIn(0f, 1f) }
}

internal val LocalPageMotion = staticCompositionLocalOf<PageMotion?> { null }

/** Slides this page by its [motion] and measures the width the slide covers. */
internal fun Modifier.slidingPage(motion: PageMotion): Modifier =
    onSizeChanged { motion.width = it.width }.graphicsLayer { translationX = motion.offset }
