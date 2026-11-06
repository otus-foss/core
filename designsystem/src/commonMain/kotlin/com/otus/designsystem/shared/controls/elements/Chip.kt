package com.otus.designsystem.shared.controls.elements

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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.controls.ControlTokens
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A compact pill inside a non-overlapping, minimum 48 dp touch target, `selectedFill` while
 * [selected]. [look] sets its fill while off. [role] says how it toggles and reads to screen
 * readers; a [ChipRole.Button] ignores [selected].
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
    role: ChipRole = ChipRole.Checkbox,
    look: ChipLook = ChipLook.Plain,
    leading: (@Composable () -> Unit)? = null,
) {
    LabeledChip(label?.let(::ChipLabel), onClick, modifier, selected, enabled, detail, role, look, leading)
}

/** A [Chip] whose [label] has a `text3` prefix, such as the parent folders of a folder whose name repeats. */
@Composable
public fun Chip(
    label: ChipLabel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    detail: String? = null,
    role: ChipRole = ChipRole.Checkbox,
    look: ChipLook = ChipLook.Plain,
    leading: (@Composable () -> Unit)? = null,
) {
    LabeledChip(label, onClick, modifier, selected, enabled, detail, role, look, leading)
}

@Composable
private fun LabeledChip(
    label: ChipLabel?,
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean,
    enabled: Boolean,
    detail: String?,
    role: ChipRole,
    look: ChipLook,
    leading: (@Composable () -> Unit)?,
) {
    val on = selected && role != ChipRole.Button
    ChipFrame(
        onClick,
        modifier.spokenAs(label),
        selected = on.takeUnless { role == ChipRole.Button },
        enabled = enabled,
        fill = look.fill,
        outline = look.outline,
        role = role,
    ) {
        leading?.invoke()
        if (label != null) ChipText(label.annotated())
        if (detail != null) {
            ChipDivider(on)
            ChipText(AnnotatedString(detail))
        }
    }
}

/** What the chip's fill is while it is off. */
public enum class ChipLook {
    /** `bgDark` with a `strokeBgDark` outline, on a page. */
    Plain,

    /** `bgRegular` with a `bgLight` outline, on a `bgDark` surface such as a filter panel. */
    Raised,
}

/** Reads a prefixed label as its spoken form, such as a folder's full path. */
private fun Modifier.spokenAs(label: ChipLabel?): Modifier =
    if (label == null ||
        label.prefix == null
    ) {
        this
    } else {
        semantics(mergeDescendants = true) { contentDescription = label.spoken }
    }

private val ChipLook.fill: Color
    @Composable get() = if (this == ChipLook.Raised) OtusColors.bgRegular else OtusColors.bgDark

private val ChipLook.outline: Color
    @Composable get() = if (this == ChipLook.Raised) OtusColors.bgLight else OtusColors.strokeBgDark

/**
 * The pill and its 48 dp touch target, shared by [Chip], [IconPill], and the other pills; [fill]
 * fills it and [outline] strokes it while unselected. [layout] sets how wide the pill is and where
 * its content starts. A chip with a [selected] state toggles as [role] says; one without, a null,
 * is a button.
 */
@Composable
internal fun ChipFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean?,
    enabled: Boolean,
    fill: Color,
    outline: Color = OtusColors.strokeBgDark,
    role: ChipRole = ChipRole.Checkbox,
    layout: ChipLayout = ChipLayout(),
    content: @Composable RowScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = Sizes.touchTarget, minHeight = Sizes.touchTarget)
            .chipAction(interactions, selected, enabled, role, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .then(layout.fillModifier())
                    .reportControlVisualBounds()
                    .controlPressFeedback(interactions),
            shape = CircleShape,
            color = if (selected == true) OtusColors.selectedFill else fill,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
            border =
                BorderStroke(
                    Sizes.borderWidth,
                    if (selected == true) OtusColors.selectedFill else outline,
                ),
        ) {
            Row(
                layout.contentModifier(),
                horizontalArrangement = layout.arrangement(),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
internal fun ChipText(text: String) {
    ChipText(AnnotatedString(text))
}

@Composable
internal fun ChipText(text: AnnotatedString) {
    Text(text, style = panelTextStyle().copy(color = Color.Unspecified), maxLines = 1)
}

/** A thin line, shorter than the text beside it, in a color that recedes behind that text. */
@Composable
internal fun ChipDivider(selected: Boolean) {
    Box(
        Modifier
            .padding(horizontal = Spacing.iconGap)
            .size(Sizes.borderWidth, ControlTokens.chipDividerHeight)
            .background(if (selected) OtusColors.text3 else OtusColors.bgLight),
    )
}

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

private fun ChipLayout.fillModifier(): Modifier = if (width == ChipWidth.Fill) Modifier.fillMaxWidth() else Modifier

/** Sizes and pads the pill's content row. */
private fun ChipLayout.contentModifier(): Modifier =
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

private fun ChipLayout.arrangement(): Arrangement.Horizontal =
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

internal fun Modifier.chipAction(
    interactions: MutableInteractionSource,
    selected: Boolean?,
    enabled: Boolean,
    role: ChipRole,
    onClick: () -> Unit,
): Modifier =
    when {
        selected == null -> {
            clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
        }

        role == ChipRole.Radio -> {
            selectable(
                selected,
                interactions,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
        }

        else -> {
            toggleable(selected, interactions, indication = null, enabled = enabled, role = Role.Checkbox) { onClick() }
        }
    }
