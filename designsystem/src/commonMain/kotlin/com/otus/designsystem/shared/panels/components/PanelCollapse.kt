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
 * How far a list has scrolled a collapsing [ContextPanel], from [progress] 0 at the top of the list
 * to 1 after [PanelTokens.collapseDistance]. Attach [connection] to the list with `nestedScroll`:
 * the panel follows the scroll itself, not a timer, so scrolling back up reverses it exactly.
 */
@Stable
public class PanelCollapse internal constructor(
    private val distance: Float,
) {
    private var scrolled by mutableFloatStateOf(0f)

    public val progress: Float
        get() = (scrolled / distance).coerceIn(0f, 1f)

    public val connection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                scrolled = (scrolled - consumed.y).coerceAtLeast(0f)
                return Offset.Zero
            }
        }

    /** Scrolls [list] to the nearer end of the collapse, so the panel never rests half collapsed. */
    internal suspend fun settle(
        list: ScrollableState,
        spec: AnimationSpec<Float>,
    ) {
        val end =
            when {
                progress <= 0f || progress >= 1f -> return
                progress < HALF -> 0f
                else -> distance
            }
        val target = end - scrolled
        // A programmatic scroll skips nested scrolling, so it moves the progress itself.
        list.scroll {
            var done = 0f
            animate(0f, target, animationSpec = spec) { value, _ ->
                val step = scrollBy(value - done)
                done += step
                scrolled = (scrolled + step).coerceAtLeast(0f)
            }
        }
        // Rounding can leave the progress a hair short of the end, which would settle again forever.
        scrolled = end
    }

    internal fun expand() {
        scrolled = 0f
    }

    private companion object {
        const val HALF = 0.5f
    }
}

/**
 * A [PanelCollapse] for [list]. Released halfway, the list settles to the nearer state on the app's
 * motion curve at the user's animation speed; back at its top, such as after a jump to the first
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

    /** The search pill tightening from 52 to 44 dp from 0.2. */
    fun search(progress: Float): Float = step(progress, SEARCH_START, 1f)

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
    private const val SEARCH_START = 0.2f
    private const val STACK_START = 0.4f
    private const val STACK_END = 0.9f
    private const val BADGE_START = 0.75f
}
