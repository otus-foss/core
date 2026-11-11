package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_add
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.settings.elements.outlineDash
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.toPath
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.selection.RuleChipTokens

/**
 * A dashed [RuleChip][com.otusfoss.core.designsystem.mail.folders.components.RuleChip]-sized button that
 * adds a rule, such as "Add rule". Its plus sits in the avatar column and [label] in the value
 * column, so both line up with the chips above it.
 */
@Composable
public fun AddRuleChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(RuleChipTokens.radius)
    Row(
        modifier
            .dashedOutline(OtusColors.bgLight)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .height(RuleChipTokens.height)
            .padding(horizontal = RuleChipTokens.padding),
        horizontalArrangement = Arrangement.spacedBy(RuleChipTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(RuleChipTokens.labelWidth))
        Box(Modifier.size(RuleChipTokens.slot), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_add) }
        }
        Text(
            label,
            color = OtusColors.text1,
            fontSize = RuleChipTokens.valueSize,
            lineHeight = RuleChipTokens.valueLineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A dashed [color] outline inside the bounds, with a rule chip's corners. */
private fun Modifier.dashedOutline(color: Color): Modifier =
    drawBehind {
        val width = Sizes.borderWidth.toPx()
        drawPath(
            SmoothCornerShape(RuleChipTokens.radius).toPath(
                Rect(width / 2, width / 2, size.width - width / 2, size.height - width / 2),
                layoutDirection,
                this,
            ),
            color,
            style = Stroke(width, pathEffect = outlineDash()),
        )
    }
