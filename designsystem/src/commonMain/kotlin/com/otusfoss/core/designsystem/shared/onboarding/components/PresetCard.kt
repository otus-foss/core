package com.otusfoss.core.designsystem.shared.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_check
import com.otusfoss.core.designsystem.resources.symbol_expand_less
import com.otusfoss.core.designsystem.resources.symbol_expand_more
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.LocalOnSelectedFill
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.onboarding.OnboardingTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/**
 * A set of settings to start from: the [preset]'s title, its marks, which show at a glance what
 * it turns on and off, and a toggle that shows the [settings] it fills in, so they can be changed
 * in place. The card is one radio button; the [selected] one fills with `selectedFill` and shows
 * a check, with the preset's note beside it.
 */
@Composable
public fun PresetCard(
    preset: PresetChoice,
    selected: Boolean,
    expanded: Boolean,
    onSelect: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    settings: @Composable ColumnScope.() -> Unit,
) {
    val shape = SmoothCornerShape(SheetTokens.radius)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .clickable(interactions, indication = null, role = Role.RadioButton, onClick = onSelect)
            .controlPressFeedback(interactions, shape)
            .background(if (selected) OtusColors.selectedFill else OtusColors.bgDark, shape)
            .strokeBorder(Sizes.borderWidth, if (selected) OtusColors.selectedFill else OtusColors.strokeBgDark, shape)
            .padding(horizontal = OnboardingTokens.cardPadding)
            // Collapsed, the toggle's touch target already keeps the bottom edge clear.
            .padding(
                top = OnboardingTokens.cardPadding,
                bottom = if (expanded) OnboardingTokens.cardPadding else Spacing.xs,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.inlineGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(preset.title, Modifier.weight(1f), fontSize = TypeScale.title, fontWeight = FontWeight.SemiBold)
            preset.note?.let { Text(it, color = OtusColors.text2, fontSize = TypeScale.label) }
            if (selected) Symbol(Res.drawable.symbol_check)
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.inlineGap),
        ) { preset.marks.forEach { Mark(it, Modifier.weight(1f)) } }
        ExpandToggle(expanded) { onExpandedChange(!expanded) }
        // The rows share the card's fill; on the selected card their outlines and switch tracks
        // take colors that show on it.
        if (expanded) {
            CompositionLocalProvider(LocalOnSelectedFill provides selected) {
                Column(verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap), content = settings)
            }
        }
    }
}

/**
 * The mark's icon in a circle of the page color, struck through from corner to corner while off,
 * over its label and the value the preset gives it.
 */
@Composable
private fun Mark(
    mark: PresetMark,
    modifier: Modifier,
) {
    Column(
        modifier.clearAndSetSemantics { contentDescription = "${mark.label}: ${mark.value}" },
        verticalArrangement = Arrangement.spacedBy(OnboardingTokens.markGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MarkIcon(mark)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mark.label, fontSize = TypeScale.detail, maxLines = 1)
            Text(
                mark.value,
                color = OtusColors.text2,
                fontSize = TypeScale.detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MarkIcon(mark: PresetMark) {
    val ink = if (mark.on) OtusColors.text1 else OtusColors.text3
    Box(
        Modifier
            .size(OnboardingTokens.markSize)
            .background(OtusColors.bgRegular, CircleShape)
            .drawWithContent {
                drawContent()
                if (!mark.on) {
                    val inset = size.minDimension * OnboardingTokens.MARK_STRIKE_INSET
                    drawLine(
                        ink,
                        Offset(inset, inset),
                        Offset(size.width - inset, size.height - inset),
                        OnboardingTokens.markStrike.toPx(),
                        StrokeCap.Round,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides ink) { Symbol(mark.icon, size = OnboardingTokens.markIcon) }
    }
}

/** "See settings" or "Hide settings", one touch target tall, with the chevron that says which way it goes. */
@Composable
private fun ExpandToggle(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
        Row(
            Modifier
                .heightIn(min = Sizes.touchTarget)
                .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(Spacing.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (expanded) "Hide settings" else "See settings", fontSize = TypeScale.label)
            Symbol(if (expanded) Res.drawable.symbol_expand_less else Res.drawable.symbol_expand_more)
        }
    }
}
