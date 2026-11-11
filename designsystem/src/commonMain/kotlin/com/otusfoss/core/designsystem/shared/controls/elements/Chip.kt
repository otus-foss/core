package com.otusfoss.core.designsystem.shared.controls.elements

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
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A compact pill inside a non-overlapping, minimum 48 dp touch target, `selectedFill` while
 * [selected]. [style] sets its fill while off and how it toggles and reads to screen readers; a
 * [ChipRole.Button] ignores [selected].
 *
 * A null [label] shows only [leading]; give the chip a content description through [modifier].
 */
@Composable
public fun Chip(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    style: ChipStyle = ChipStyle(),
    leading: (@Composable () -> Unit)? = null,
) {
    Chip(ChipLabel(label), onClick, modifier, selected, enabled, style, leading)
}

/** A [Chip] whose [label] may have a `text3` prefix or a detail after a divider line. */
@Composable
public fun Chip(
    label: ChipLabel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    style: ChipStyle = ChipStyle(),
    leading: (@Composable () -> Unit)? = null,
) {
    val on = selected && style.role != ChipRole.Button
    ChipFrame(
        onClick,
        modifier.spokenAs(label),
        selected = on.takeUnless { style.role == ChipRole.Button },
        enabled = enabled,
        style = style.frame,
    ) {
        leading?.invoke()
        if (label.text != null) ChipText(label.annotated())
        if (label.detail != null) {
            ChipDivider(on)
            ChipText(AnnotatedString(label.detail))
        }
    }
}

/** How a [Chip] toggles and reads ([role]) and what its fill is while off ([look]). */
public data class ChipStyle(
    val role: ChipRole = ChipRole.Checkbox,
    val look: ChipLook = ChipLook.Plain,
)

/** What the chip's fill is while it is off. */
public enum class ChipLook {
    /** `bgDark` with a `strokeBgDark` outline, on a page. */
    Plain,

    /** `bgRegular` with a `bgLight` outline, on a `bgDark` surface such as a filter panel. */
    Raised,
}

/** Reads a prefixed label as its spoken form, such as a folder's full path. */
private fun Modifier.spokenAs(label: ChipLabel): Modifier =
    if (label.prefix == null) this else semantics(mergeDescendants = true) { contentDescription = label.spoken }

private val ChipStyle.frame: ChipFrameStyle
    @Composable get() =
        if (look == ChipLook.Raised) {
            ChipFrameStyle(OtusColors.bgRegular, OtusColors.bgLight, role)
        } else {
            ChipFrameStyle(OtusColors.bgDark, role = role)
        }

/** A [ChipFrame]'s [fill], the [outline] that strokes it while unselected, and how it toggles. */
internal data class ChipFrameStyle(
    val fill: Color,
    val outline: Color = OtusColors.strokeBgDark,
    val role: ChipRole = ChipRole.Checkbox,
)

/**
 * The pill and its 48 dp touch target, shared by [Chip], [IconPill], and the other pills, in
 * [style] while unselected. [layout] sets how wide the pill is and where
 * its content starts. A chip with a [selected] state toggles as the style's role says; one without, a null,
 * is a button.
 */
@Composable
internal fun ChipFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean?,
    enabled: Boolean,
    style: ChipFrameStyle,
    layout: ChipLayout = ChipLayout(),
    content: @Composable RowScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = Sizes.touchTarget, minHeight = Sizes.touchTarget)
            .chipAction(interactions, selected, enabled, style.role, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .then(layout.fillModifier())
                    .reportControlVisualBounds()
                    .controlPressFeedback(interactions),
            shape = CircleShape,
            color = if (selected == true) OtusColors.selectedFill else style.fill,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
            border =
                BorderStroke(
                    Sizes.borderWidth,
                    if (selected == true) OtusColors.selectedFill else style.outline,
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
