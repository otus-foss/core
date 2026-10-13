package com.otus.designsystem.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.motionTween

/**
 * Where a full bar drew its shell and action, as widths from the bar's end edge, and which
 * [context] it drew how far faded in. Hosted bars share the screen's width, so these carry from
 * one to the next.
 */
internal data class BarGeometry(
    val shellWidth: Int,
    val actionWidth: Int,
    val progress: Float,
    val context: DrawnContext?,
)

/** A context as a bar draws it, under its primary's [title]. */
internal data class DrawnContext(
    val title: String,
    val context: BarContext,
)

/**
 * How a hosted bar takes over from the one in front. While another bar is in front, such as
 * before this one first comes forward or while a page covers it, this bar is drawn where the
 * front bar is. Once it comes forward, its shell, action, and context move from there to their
 * own places in one motion, however the two bars' secondary actions and contexts differ. A bar
 * outside an [ActionBarHost] stays in place.
 */
internal class BarEntry(
    private val hosted: Boolean,
) {
    private var share by mutableFloatStateOf(if (hosted) 0f else 1f)
    private var start: BarGeometry? = null
    private var inFront = false
    private var kept: DrawnContext? = null

    /** Whether this bar has a bar to move in from, read once when it is created. */
    val movesIn: Boolean = hosted && Snapshot.withoutReadObservation { FrontGeometry.last } != null

    /** [own] for this bar, blended from where it moves in from. Reads layout state. */
    fun blend(
        own: Float,
        of: (BarGeometry) -> Float,
    ): Float {
        val from = if (inFront) start else FrontGeometry.last.takeIf { hosted }
        return from?.let { of(it) + (own - of(it)) * share } ?: own
    }

    /**
     * The context to draw: [own] under [title], or while that fades out, the last one drawn. A bar
     * that moves in fades out the context of the bar it started from, under that bar's title.
     */
    fun shownContext(
        own: BarContext?,
        title: String,
    ): DrawnContext? {
        if (own != null) kept = DrawnContext(title, own)
        return kept
    }

    /** Keeps where this bar is drawn while it is in front, for the next bar to start from. */
    fun record(
        shellWidth: Int,
        actionWidth: Int,
        progress: Float,
    ) {
        val geometry = BarGeometry(shellWidth, actionWidth, progress, kept)
        if (inFront && Snapshot.withoutReadObservation { FrontGeometry.last } != geometry) FrontGeometry.last = geometry
    }

    /** Follows whether this bar is [front] on each composition. It starts from the front bar once it comes forward. */
    fun follow(front: Boolean) {
        if (!hosted || front == inFront) return
        inFront = front
        if (!front) {
            share = 0f
            return
        }
        start = Snapshot.withoutReadObservation { FrontGeometry.last }
        kept = start?.context
    }

    suspend fun moveIn(spec: AnimationSpec<Float>) {
        animate(share, 1f, animationSpec = spec) { value, _ -> share = value }
    }
}

@Composable
internal fun rememberBarEntry(front: FrontBar): BarEntry {
    val hosted = LocalHostedBar.current
    val entry = remember { BarEntry(hosted) }
    entry.follow(front.inFront)
    val move = motionTween<Float>(MotionSpeed.Fast)
    LaunchedEffect(front.inFront) { if (front.inFront) entry.moveIn(move) }
    return entry
}

/** Where the frontmost full bar was last drawn. It is app-wide, like the bars it follows. */
private object FrontGeometry {
    var last: BarGeometry? by mutableStateOf(null)
}
