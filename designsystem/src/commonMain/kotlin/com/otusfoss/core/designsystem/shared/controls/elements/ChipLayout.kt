package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing

/**
 * How a [ChipFrame] is laid out: its [width], whether a round avatar comes first, close to the
 * start edge, its [height], and the content inset from its [start] and [end].
 */
internal data class ChipLayout(
    val width: ChipWidth = ChipWidth.Content,
    val avatarFirst: Boolean = false,
    val height: Dp = Sizes.chipHeight,
    val start: Dp = ControlTokens.chipPadding,
    val end: Dp = ControlTokens.chipPadding,
)

internal fun ChipLayout.fillModifier(): Modifier = if (width == ChipWidth.Fill) Modifier.fillMaxWidth() else Modifier

/** Sizes and pads the pill's content row. */
internal fun ChipLayout.contentModifier(): Modifier =
    if (width == ChipWidth.Circle) {
        Modifier.size(height)
    } else {
        fillModifier()
            .heightIn(min = height)
            .padding(
                start = if (avatarFirst) Spacing.iconGap else start,
                end = end,
                top = Spacing.iconGap,
                bottom = Spacing.iconGap,
            )
    }

internal fun ChipLayout.arrangement(): Arrangement.Horizontal =
    if (width == ChipWidth.Fill) LastAtEnd else Arrangement.spacedBy(Spacing.iconGap, Alignment.CenterHorizontally)

/** How wide a [ChipFrame] is. */
internal enum class ChipWidth {
    /** As wide as its content. */
    Content,

    /** As wide as it is tall, like an [IconButton]. */
    Circle,

    /** The width its modifier gives it, with its last item at the end. */
    Fill,
}

/** Packs items from the start [Spacing.iconGap] apart, except the last, which sits at the end. */
private object LastAtEnd : Arrangement.Horizontal {
    override val spacing = Spacing.iconGap

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray,
    ) {
        val gap = spacing.roundToPx()
        var start = 0
        sizes.forEachIndexed { index, size ->
            val position = if (index == sizes.lastIndex) totalSize - size else start
            outPositions[index] = if (layoutDirection == LayoutDirection.Ltr) position else totalSize - position - size
            start += size + gap
        }
    }
}
