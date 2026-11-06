package com.otus.designsystem.shared.pages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * How far up from the window's bottom the bars on screen reach, so parts that float over every
 * page, such as toasts, can sit above them. Each
 * [ActionBarHost][com.otus.designsystem.shared.actionbar.components.ActionBarHost] with a bar
 * reports its height here while it is composed. Provide one with [LocalBottomClearance] around
 * the pages and what floats over them.
 */
@Stable
public class BottomClearance {
    private val heights = mutableStateMapOf<Any, Dp>()

    /** The highest bar on screen, or 0 dp without one; while a page slides in, both pages' bars count. */
    public val height: Dp get() = heights.values.maxOrNull() ?: 0.dp

    internal fun report(
        key: Any,
        height: Dp,
    ) {
        heights[key] = height
    }

    internal fun remove(key: Any) {
        heights.remove(key)
    }
}

public val LocalBottomClearance: ProvidableCompositionLocal<BottomClearance?> =
    staticCompositionLocalOf { null }

/** Reports [height] to the [LocalBottomClearance] until this leaves the composition. */
@Composable
internal fun ReportClearance(height: Dp) {
    val clearance = LocalBottomClearance.current ?: return
    val key = remember { Any() }
    SideEffect { clearance.report(key, height) }
    DisposableEffect(clearance) { onDispose { clearance.remove(key) } }
}

/**
 * Places a floating part as low as it can go: [ActionBarTokens.barInset] above the frontmost bar
 * in [LocalBottomClearance], or above the navigation bar and keyboard when no bar is on screen.
 * It moves between the two at the normal speed. Over a bar, anything it draws below the bar's top
 * edge is hidden, as if behind the bar. Use it on a part aligned to the window's bottom.
 */
@Composable
internal fun Modifier.aboveBottomBars(): Modifier {
    val bars by animateDpAsState(
        LocalBottomClearance.current?.height ?: 0.dp,
        motionTween(MotionSpeed.Normal),
        label = "Bottom clearance",
    )
    val edge =
        WindowInsets.navigationBars
            .union(WindowInsets.ime)
            .asPaddingValues()
            .calculateBottomPadding() + ActionBarTokens.barInset
    val bottom = maxOf(bars, edge)
    if (bars == 0.dp) return padding(bottom = bottom)
    // What sinks past the bar's top edge, such as a leaving toast, goes behind the bar.
    return drawWithContent {
        clipRect(-size.width, -size.height, size.width * 2, size.height - (bars - ActionBarTokens.barInset).toPx()) {
            this@drawWithContent.drawContent()
        }
    }.padding(bottom = bottom)
}
