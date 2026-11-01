package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import kotlin.math.sqrt

/**
 * Pulling down on this part opens the next step, such as the control panel's quick view: past
 * [QuickViewTokens.pullDistance] when the finger lifts, [onPull] runs. While pulled the part stretches a
 * little, with resistance, so the drag feels attached; an upward drag does nothing here. A null
 * [onPull] turns the pull off.
 */
@Composable
public fun Modifier.pullDown(onPull: (() -> Unit)?): Modifier {
    if (onPull == null) return this
    val density = LocalDensity.current
    val distance = with(density) { QuickViewTokens.pullDistance.toPx() }
    val stretch = with(density) { QuickViewTokens.pullStretch.toPx() }
    var pulled by remember { mutableFloatStateOf(0f) }
    return this
        .graphicsLayer { translationY = resisted(pulled, distance, stretch) }
        .draggable(
            rememberDraggableState { pulled = (pulled + it).coerceAtLeast(0f) },
            Orientation.Vertical,
            onDragStopped = { velocity ->
                if (pulled >= distance || velocity > distance * FLING_PER_DISTANCE) onPull()
                pulled = 0f
            },
        )
}

/** [pulled] eased to at most [stretch], so the part follows the finger less the further it goes. */
internal fun resisted(
    pulled: Float,
    distance: Float,
    stretch: Float,
): Float = if (pulled <= 0f) 0f else stretch * sqrt((pulled / distance).coerceAtMost(1f))

// A downward fling faster than this many pull distances a second opens without reaching it.
private const val FLING_PER_DISTANCE = 12f
