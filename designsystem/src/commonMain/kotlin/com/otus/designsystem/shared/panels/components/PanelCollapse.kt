package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import kotlinx.coroutines.launch

/**
 * How far a list has scrolled a collapsing [ContextPanel] away, from [progress] 0 to 1 over
 * [PanelTokens.collapseDistance]. Attach [connection] to the list with `nestedScroll`: the panel
 * follows the scroll itself, not a timer, so scrolling up anywhere in the list brings it back.
 */
@Stable
public class PanelCollapse internal constructor(
    private val distance: Float,
) {
    // How far the list sits from its top, which tells whether settling must move the list.
    private var offset by mutableFloatStateOf(0f)
    private var hidden by mutableFloatStateOf(0f)
    private var scrollingUp = false

    public val progress: Float
        get() = hidden / distance

    public val connection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (consumed.y != 0f) move(-consumed.y)
                return Offset.Zero
            }
        }

    private fun move(delta: Float) {
        scrollingUp = delta < 0f
        offset = (offset + delta).coerceAtLeast(0f)
        hidden = (hidden + delta).coerceIn(0f, distance)
    }

    /**
     * Finishes a half collapse. Near the top the list scrolls to the nearer end, so the first row
     * meets the panel's edge; further down only the panel moves, the way the last scroll went.
     */
    internal suspend fun settle(
        list: ScrollableState,
        spec: AnimationSpec<Float>,
    ) {
        if (progress <= 0f || progress >= 1f) return
        val end =
            when {
                offset > distance -> if (scrollingUp) 0f else distance
                progress < HALF -> 0f
                else -> distance
            }
        // Inside a list scroll so a touch interrupts it. A programmatic scroll skips nested
        // scrolling, so it moves the progress itself.
        list.scroll {
            if (offset > distance) {
                animate(hidden, end, animationSpec = spec) { value, _ -> hidden = value }
                return@scroll
            }
            var done = 0f
            animate(0f, end - hidden, animationSpec = spec) { value, _ ->
                val step = scrollBy(value - done)
                done += step
                move(step)
            }
        }
        // Rounding can leave the progress a hair short of the end, which would settle again forever.
        hidden = end
    }

    internal fun expand() {
        offset = 0f
        hidden = 0f
    }

    private companion object {
        const val HALF = 0.5f
    }
}

/**
 * A [PanelCollapse] for [list]. Released halfway, the panel settles on the app's motion curve at
 * the user's animation speed, as [PanelCollapse.settle] describes; back at its top, such as after a jump to the first
 * row, the panel is fully expanded.
 */
@Composable
public fun rememberPanelCollapse(list: ScrollableState): PanelCollapse {
    val distance = with(LocalDensity.current) { PanelTokens.collapseDistance.toPx() }
    val collapse = remember(list, distance) { PanelCollapse(distance) }
    val spec = motionTween<Float>(curve = MotionCurve.Standard)
    LaunchedEffect(collapse, spec) {
        snapshotFlow { list.isScrollInProgress to list.canScrollBackward }.collect { (scrolling, scrolledDown) ->
            when {
                !scrolledDown -> collapse.expand()

                // Its own job, so a touch that interrupts the settle leaves this watch running.
                !scrolling -> launch { collapse.settle(list, spec) }
            }
        }
    }
    return collapse
}

/**
 * Where each part of a collapsing panel is at a collapse [progress]. Each part runs over its own
 * slice of the scroll, eased on the app's standard curve.
 */
internal object CollapseSteps {
    /** The pills riding the edge, then rising, shrinking, and fading out by 0.6. */
    fun pills(progress: Float): Float = step(progress, 0f, PILLS_END)

    /** The account stack folding into one avatar from 0.4. */
    fun stack(progress: Float): Float = step(progress, STACK_START, STACK_END)

    /** The "+N" badge on the folded avatar, at the end of the fold. */
    fun badge(progress: Float): Float = linear(progress, BADGE_START, 1f)

    /** The filter button growing into the search pill from 0.6. */
    fun filter(progress: Float): Float = step(progress, PILLS_END, 1f)

    private fun step(
        progress: Float,
        start: Float,
        end: Float,
    ): Float = MotionCurve.Standard.easing.transform(linear(progress, start, end))

    private fun linear(
        progress: Float,
        start: Float,
        end: Float,
    ): Float = ((progress - start) / (end - start)).coerceIn(0f, 1f)

    private const val PILLS_END = 0.6f
    private const val STACK_START = 0.4f
    private const val STACK_END = 0.9f
    private const val BADGE_START = 0.75f
}
