package com.otus.designsystem.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A compact pill inside a non-overlapping, minimum 48 dp touch target.
 *
 * A null [label] shows only [leading]; give the chip a content description through [modifier].
 * [detail] follows after a short divider line, such as the mailboxes beside an account.
 */
@Composable
public fun Chip(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    detail: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    ChipFrame(onClick, modifier, selected, enabled, OtusColors.bgDark) {
        leading?.invoke()
        label?.let { ChipText(it) }
        detail?.let {
            ChipDivider(selected)
            ChipText(it)
        }
    }
}

/**
 * The pill and its 48 dp touch target, shared by [Chip], [SidebarPill], [IconPill], and the
 * button pills; [fill] fills it while unselected. [layout] sets how wide the pill is and where
 * its content starts. A chip with a [selected]
 * state reads as a checkbox; one without, a null, is a button.
 */
@Composable
internal fun ChipFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean?,
    enabled: Boolean,
    fill: Color,
    layout: ChipLayout = ChipLayout(),
    content: @Composable RowScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = PanelTokens.touchTarget, minHeight = PanelTokens.touchTarget)
            .then(
                if (selected == null) {
                    Modifier.clickable(interactions, null, enabled, role = Role.Button, onClick = onClick)
                } else {
                    Modifier.toggleable(selected, interactions, null, enabled, Role.Checkbox) { onClick() }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .then(if (layout.width == ChipWidth.Fill) Modifier.fillMaxWidth() else Modifier)
                    .reportControlVisualBounds()
                    .controlPressFeedback(interactions),
            shape = CircleShape,
            color = if (selected == true) OtusColors.accentDark else fill,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
            border =
                BorderStroke(
                    PanelTokens.borderWidth,
                    if (selected == true) OtusColors.accentLight else OtusColors.strokeBgDark,
                ),
        ) {
            Row(
                if (layout.width == ChipWidth.Circle) {
                    Modifier.size(layout.height)
                } else {
                    Modifier
                        .then(if (layout.width == ChipWidth.Fill) Modifier.fillMaxWidth() else Modifier)
                        .heightIn(min = layout.height)
                        .padding(
                            start = if (layout.avatarFirst) PanelTokens.iconGap else layout.start,
                            end = layout.end,
                            top = PanelTokens.iconGap,
                            bottom = PanelTokens.iconGap,
                        )
                },
                horizontalArrangement =
                    if (layout.width == ChipWidth.Fill) {
                        LastAtEnd
                    } else {
                        Arrangement.spacedBy(PanelTokens.iconGap, Alignment.CenterHorizontally)
                    },
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
internal fun ChipText(text: String) {
    Text(text, style = panelTextStyle().copy(color = Color.Unspecified), maxLines = 1)
}

/** A thin line, shorter than the text beside it, in a color that recedes behind that text. */
@Composable
private fun ChipDivider(selected: Boolean) {
    Box(
        Modifier
            .padding(horizontal = PanelTokens.iconGap)
            .size(PanelTokens.borderWidth, PanelTokens.chipDividerHeight)
            .background(if (selected) OtusColors.accentLight else OtusColors.bgLight),
    )
}

/**
 * How a [ChipFrame] is laid out: its [width], whether a round avatar comes first, close to the
 * start edge, its [height], and the content inset from its [start] and [end].
 */
internal data class ChipLayout(
    val width: ChipWidth = ChipWidth.Content,
    val avatarFirst: Boolean = false,
    val height: Dp = PanelTokens.chipHeight,
    val start: Dp = PanelTokens.chipPadding,
    val end: Dp = PanelTokens.chipPadding,
)

/** How wide a [ChipFrame] is. */
internal enum class ChipWidth {
    /** As wide as its content. */
    Content,

    /** As wide as it is tall, like an [IconButton]. */
    Circle,

    /** The width its modifier gives it, with its last item at the end. */
    Fill,
}

/** Packs items from the start [PanelTokens.iconGap] apart, except the last, which sits at the end. */
private object LastAtEnd : Arrangement.Horizontal {
    override val spacing = PanelTokens.iconGap

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
