package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * How far a [ContextPanel] has grown below its header row, which the user drags: the panel follows
 * the finger, then settles on the nearest of its [stops] when the finger lifts, or on the next one
 * the way a fling went. Stop 0 is the panel as it always is. Code opens a stop with [expandTo].
 */
@Stable
public class PanelExpansion internal constructor(
    private val scope: CoroutineScope,
) {
    internal var stops: List<Float> = listOf(0f)
    private var motionScale = 1f
    private var flingSpeed = 0f
    private var running: Job? = null

    /** The extra height in pixels, between 0 and the last stop. */
    internal var height by mutableFloatStateOf(0f)
        private set

    /** The stop the panel last settled on or is heading to. */
    public var stop: Int by mutableIntStateOf(0)
        private set

    /**
     * Where the panel is between its stops: 0 at the first, 1 at the second, 1.5 halfway to the
     * third. Content that shows at a stop fades with it.
     */
    public val position: Float
        get() {
            val next = stops.indexOfFirst { it >= height }.coerceAtLeast(1)
            if (stops.size < 2) return 0f
            val start = stops[next - 1]
            val span = (stops[next] - start).takeIf { it > 0f } ?: return next.toFloat()
            return (next - 1) + ((height - start) / span).coerceIn(0f, 1f)
        }

    /** Whether the panel stands out past its first stop, so what is behind it should not take taps. */
    public val expanded: Boolean
        get() = height > 0f

    /** Moves to [target] on the app's motion curve, such as when a button opens the full panel. */
    public fun expandTo(target: Int) {
        moveTo(target, Motion.tween(scale = motionScale))
    }

    private fun moveTo(
        target: Int,
        spec: AnimationSpec<Float>,
    ) {
        val index = target.coerceIn(0, stops.lastIndex)
        stop = index
        running?.cancel()
        running = scope.launch { animate(height, stops[index], animationSpec = spec) { value, _ -> height = value } }
    }

    /** Back to the panel as it always is. */
    public fun collapse(): Unit = expandTo(0)

    internal fun drag(delta: Float) {
        running?.cancel()
        height = (height + delta).coerceIn(0f, stops.last())
    }

    internal fun release(velocity: Float) {
        val nearest = stops.indices.minBy { abs(stops[it] - height) }
        val target =
            when {
                velocity > flingSpeed -> stops.indexOfFirst { it > height }.takeIf { it >= 0 } ?: stops.lastIndex
                velocity < -flingSpeed -> stops.indexOfLast { it < height }.coerceAtLeast(0)
                else -> nearest
            }
        // The panel is already moving, so it carries on at once, decelerating, for only as long as
        // the distance left needs.
        val span = (stops.zipWithNext { a, b -> b - a }.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val left = (abs(stops[target] - height) / span).coerceIn(SHORTEST_SETTLE, 1f)
        val millis = Motion.millis(MotionSpeed.Normal, motionScale) * left
        moveTo(target, TweenSpec(millis.roundToInt(), easing = MotionCurve.Enter.easing))
    }

    /** Hands scrolling that content inside the panel leaves over to the panel, so it follows on. */
    internal val connection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                val before = height
                drag(available.y)
                return Offset(0f, height - before)
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (height !in stops) release(available.y)
                return Velocity.Zero
            }
        }

    internal fun configure(
        stops: List<Float>,
        motionScale: Float,
        flingSpeed: Float,
    ) {
        this.stops = listOf(0f) + stops.filter { it > 0f }.sorted()
        this.motionScale = motionScale
        this.flingSpeed = flingSpeed
        if (height > this.stops.last()) height = this.stops.last()
    }
}

/**
 * A [PanelExpansion] whose stops, after the panel as it always is, are [stops] of extra height in
 * order, such as a quick view and then every filter. It settles on the app's motion curve at the
 * user's animation speed.
 */
@Composable
public fun rememberPanelExpansion(stops: List<Dp>): PanelExpansion {
    val scope = rememberCoroutineScope()
    val expansion = remember(scope) { PanelExpansion(scope) }
    val density = LocalDensity.current
    val motionScale = LocalMotionScale.current
    with(density) {
        expansion.configure(stops.map { it.toPx() }, motionScale, QuickViewTokens.flingSpeed.toPx())
    }
    return expansion
}

/** The shortest settle, as a share of a full one, so a release next to a stop still eases in. */
private const val SHORTEST_SETTLE = 0.35f
