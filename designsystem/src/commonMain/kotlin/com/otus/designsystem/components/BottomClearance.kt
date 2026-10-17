package com.otus.designsystem.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.motionTween

/**
 * How far up from the window's bottom the bars on screen reach, so parts that float over every
 * page, such as toasts, can sit above them. Each [ActionBarHost] with a bar reports its height
 * here while it is composed. Provide one with [LocalBottomClearance] around the pages and what
 * floats over them.
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
 * It moves between the two at the normal speed.
 */
@Composable
public fun Modifier.aboveBottomBars(): Modifier {
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
    return padding(bottom = maxOf(bars, edge))
}
