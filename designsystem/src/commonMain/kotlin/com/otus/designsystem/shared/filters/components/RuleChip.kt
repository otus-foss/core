package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.shared.rows.components.RowDensity
import com.otus.designsystem.shared.rows.components.topHairline
import com.otus.designsystem.shared.selection.components.valueOutline
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.selection.RuleChipTokens

/**
 * One rule condition in an outline: a fixed label column, a slot with an avatar or grey icon, and
 * the value, which ends in an ellipsis when it is too long. A [RowDensity.Compact] chip is 24 dp
 * tall for a folder row: its label takes only the width it needs, its avatar is 16 dp, and it
 * leaves out the icon. With [onClick], the whole chip is one button. It fits its content unless
 * [modifier] gives it a width.
 */
@Composable
public fun RuleChip(
    line: RuleChipLine,
    modifier: Modifier = Modifier,
    density: RowDensity = RowDensity.Regular,
    onClick: (() -> Unit)? = null,
) {
    val compact = density == RowDensity.Compact
    val shape = RoundedCornerShape(if (compact) RuleChipTokens.compactRadius else RuleChipTokens.radius)
    RuleLineRow(
        line,
        compact,
        modifier
            .valueOutline(shape)
            .ruleTarget(onClick, shape)
            .height(if (compact) RuleChipTokens.compactHeight else RuleChipTokens.height)
            .padding(horizontal = if (compact) RuleChipTokens.compactPadding else RuleChipTokens.padding),
    )
}

/** Fades a [RuleChip] or [RuleChipGroup] shown only for context, such as a parent folder's rule. */
public fun Modifier.mutedRule(): Modifier = alpha(RuleChipTokens.MUTED_ALPHA)

/**
 * Several conditions that must all match, such as an "All of" rule: one outline around [lines],
 * the small [title] on top, such as "ALL OF", and hairlines between the conditions. The columns
 * line up with single [RuleChip]s. With [onClick], the whole group is one button.
 */
@Composable
public fun RuleChipGroup(
    title: String,
    lines: List<RuleChipLine>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(RuleChipTokens.radius)
    Column(
        modifier
            .valueOutline(shape)
            .ruleTarget(onClick, shape)
            .padding(top = RuleChipTokens.groupTitleTop),
    ) {
        Text(
            title,
            Modifier.padding(horizontal = RuleChipTokens.padding),
            color = OtusColors.text3,
            fontSize = RuleChipTokens.groupTitleSize,
            lineHeight = RuleChipTokens.groupTitleLineHeight,
            letterSpacing = RuleChipTokens.groupTitleTracking,
            maxLines = 1,
        )
        lines.forEachIndexed { index, line ->
            RuleLineRow(
                line,
                compact = false,
                Modifier
                    .fillMaxWidth()
                    .then(if (index > 0) Modifier.topHairline(OtusColors.bgLight) else Modifier)
                    .height(RuleChipTokens.height)
                    .padding(horizontal = RuleChipTokens.padding),
            )
        }
    }
}

@Composable
private fun RuleLineRow(
    line: RuleChipLine,
    compact: Boolean,
    modifier: Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) RuleChipTokens.compactGap else RuleChipTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            line.label,
            if (compact) Modifier else Modifier.width(RuleChipTokens.labelWidth),
            color = OtusColors.text2,
            fontSize = RuleChipTokens.labelSize,
            lineHeight = RuleChipTokens.labelLineHeight,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (compact) {
            line.avatar?.let { PersonAvatar(it, RuleChipTokens.compactAvatar) }
        } else {
            RuleSlot(line)
        }
        RuleValue(
            line.value,
            Modifier.weight(1f, fill = false),
            if (compact) RuleChipTokens.compactValueSize else RuleChipTokens.valueSize,
            if (compact) RuleChipTokens.compactValueLineHeight else RuleChipTokens.valueLineHeight,
        )
    }
}

/** The 20 dp slot: the avatar, the grey icon, or empty space that keeps the value in its column. */
@Composable
private fun RuleSlot(line: RuleChipLine) {
    Box(Modifier.size(RuleChipTokens.slot), contentAlignment = Alignment.Center) {
        when {
            line.avatar != null -> {
                PersonAvatar(line.avatar, RuleChipTokens.slot)
            }

            line.icon != null -> {
                CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                    Symbol(line.icon, size = RuleChipTokens.slotIcon)
                }
            }
        }
    }
}

@Composable
private fun RuleValue(
    value: RuleChipValue,
    modifier: Modifier,
    fontSize: TextUnit,
    lineHeight: TextUnit,
) {
    Text(
        value.annotated(),
        modifier,
        color = OtusColors.text1,
        fontSize = fontSize,
        lineHeight = lineHeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RuleChipValue.annotated(): AnnotatedString {
    val dim = OtusColors.text3
    val bright = OtusColors.text1
    val accent = OtusColors.accentLight
    return buildAnnotatedString {
        segments.forEach { segment ->
            val color =
                when (segment.style) {
                    RuleSegmentStyle.Dim -> dim
                    RuleSegmentStyle.Bright -> bright
                    RuleSegmentStyle.Accent -> accent
                }
            withStyle(SpanStyle(color = color)) { append(segment.text) }
        }
    }
}

/** Makes a rule one button with [onClick], or without it, one element screen readers read whole. */
@Composable
private fun Modifier.ruleTarget(
    onClick: (() -> Unit)?,
    shape: Shape,
): Modifier {
    if (onClick == null) return semantics(mergeDescendants = true) {}
    val interactions = remember { MutableInteractionSource() }
    return clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
        .controlPressFeedback(interactions, shape)
}
