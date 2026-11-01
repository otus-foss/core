package com.otus.designsystem.shared.rows.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_less
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.rows.elements.SectionCaption
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * How far a [FoldingSection] has folded away. Put [connection] on what holds the section and a
 * list under it: scrolling the list toward its end folds the section first, and pulling the list
 * past its top unfolds it.
 */
@Stable
public class SectionFold internal constructor(
    private val scope: CoroutineScope,
) {
    /** The content's unfolded height in pixels, as last measured. */
    internal var full by mutableIntStateOf(0)

    /** How many pixels of the content are folded away. */
    internal var folded by mutableFloatStateOf(0f)
        private set

    internal var motionScale = 1f
    private var running: Job? = null

    /** 0 while the content shows in full, 1 once it has folded away. */
    public val progress: Float
        get() = if (full == 0) 0f else (folded / full).coerceIn(0f, 1f)

    public val connection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset = if (available.y < 0f) Offset(0f, -take(-available.y)) else Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = if (available.y > 0f) Offset(0f, give(available.y)) else Offset.Zero
        }

    /** Folds the content away if it mostly shows, else unfolds it, on the motion curve. */
    public fun toggle() {
        val target = if (progress < HALF) full.toFloat() else 0f
        running?.cancel()
        running =
            scope.launch {
                animate(
                    folded,
                    target,
                    animationSpec = Motion.tween(scale = motionScale),
                ) { value, _ -> folded = value }
            }
    }

    private fun take(distance: Float): Float {
        val step = distance.coerceAtMost(full - folded).coerceAtLeast(0f)
        if (step > 0f) running?.cancel()
        folded += step
        return step
    }

    private fun give(distance: Float): Float {
        val step = distance.coerceAtMost(folded)
        if (step > 0f) running?.cancel()
        folded -= step
        return step
    }
}

@Composable
public fun rememberSectionFold(): SectionFold {
    val scope = rememberCoroutineScope()
    val fold = remember(scope) { SectionFold(scope) }
    fold.motionScale = LocalMotionScale.current
    return fold
}

/**
 * A [title] caption over [content] that folds away by [fold]: the content shrinks, fades, and gives
 * up its height as a list under it scrolls on, leaving the caption, whose chevron folds or unfolds
 * it with a tap.
 */
@Composable
public fun FoldingSection(
    title: String,
    fold: SectionFold,
    captionModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column {
        SectionCaption(title, captionModifier) {
            IconButton(
                if (fold.progress < HALF) "Hide $title" else "Show $title",
                fold::toggle,
                look = IconButtonLook.Bare,
            ) {
                Box(Modifier.graphicsLayer { rotationZ = HALF_TURN * fold.progress }) {
                    Symbol(Res.drawable.symbol_expand_less)
                }
            }
        }
        Column(Modifier.clipToBounds().folding(fold)) { content() }
    }
}

/** Lays the content out shorter by what [fold] has folded, shrinking and fading it from the top. */
private fun Modifier.folding(fold: SectionFold): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        fold.full = placeable.height
        val height = (placeable.height - fold.folded).roundToInt().coerceAtLeast(0)
        layout(placeable.width, height) {
            placeable.placeWithLayer(0, 0) {
                val progress = fold.progress
                alpha = 1f - progress
                scaleX = lerp(1f, FOLDED_SCALE, progress)
                scaleY = scaleX
                transformOrigin = TransformOrigin(HALF, 0f)
            }
        }
    }

private const val HALF = 0.5f
private const val HALF_TURN = 180f

/** How small folded content gets before it is gone. */
private const val FOLDED_SCALE = 0.85f
