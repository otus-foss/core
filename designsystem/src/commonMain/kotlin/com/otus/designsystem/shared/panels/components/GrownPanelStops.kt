package com.otus.designsystem.shared.panels.components

import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens

// The stops a page's control panel grows through: regular, its usual height (stop 0); expanded,
// the quick view; and full, the last stop, such as every filter or every detail.

/** A grown panel's expanded stop, with the quick view. */
public const val GROWN_EXPANDED_STOP: Int = 1

/** A grown panel's full stop, its last, such as every filter. */
public const val GROWN_LAST_STOP: Int = 2

/** Where the last stop takes over from the quick view, halfway to it. */
public const val GROWN_LAST_STOP_FROM: Float = 1.5f

/** Where the quick view starts to show, as the panel leaves its usual height. */
private const val QUICK_FROM = 0.3f

/** A quick view's height with [modules] picked: the modules, its last row, and the grab handle. */
public fun quickViewHeight(modules: Int): Dp =
    grownStopHeight(
        QuickViewTokens.moduleGap +
            (QuickViewTokens.moduleHeight + QuickViewTokens.moduleGap) * modules +
            QuickViewTokens.jumpHeight,
    )

/** A stop's height for [content] of a measured height, with the grab handle below it. */
public fun grownStopHeight(content: Dp): Dp = content + PanelTokens.handlePadding * 2 + PanelTokens.handleHeight

/** How much a grown panel's quick view shows at [position]: in toward its stop, out toward the next. */
internal fun quickViewAlpha(position: Float): Float =
    when {
        position <= QUICK_FROM -> 0f
        position <= 1f -> (position - QUICK_FROM) / (1f - QUICK_FROM)
        else -> (1f - (position - 1f) / (GROWN_LAST_STOP_FROM - 1f)).coerceAtLeast(0f)
    }

/**
 * How much what both stops show stays at [position]: in with the quick view and held through the
 * last stop, or in with the last stop when the panel [skipping] past the quick view.
 */
internal fun sharedAlpha(
    position: Float,
    skipping: Boolean,
): Float =
    when {
        skipping -> lastStopAlpha(position)
        position >= 1f -> 1f
        else -> quickViewAlpha(position)
    }

/** How much a grown panel's last stop shows at [position]. */
internal fun lastStopAlpha(position: Float): Float =
    ((position - GROWN_LAST_STOP_FROM) / (GROWN_LAST_STOP - GROWN_LAST_STOP_FROM)).coerceIn(0f, 1f)
