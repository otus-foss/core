package com.otus.designsystem.shared.settings.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_add
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * One color to pick, such as an accent. A selected swatch shows a check and a [OtusColors.text1]
 * ring. [label] names the color for screen readers.
 */
@Composable
public fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(ThemeTokens.swatchTargetWidth, PanelTokens.touchTarget)
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, CircleShape)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(ThemeTokens.swatchSize + ThemeTokens.swatchRingSpace * 2)
                .then(
                    if (selected) Modifier.border(ThemeTokens.swatchRing, OtusColors.text1, CircleShape) else Modifier,
                ).padding(ThemeTokens.swatchRingSpace)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                CompositionLocalProvider(LocalContentColor provides ThemeTokens.onSwatch) {
                    Symbol(Res.drawable.symbol_check, size = ThemeTokens.swatchCheck)
                }
            }
        }
    }
}

/** A dashed circle with a plus that opens a custom color; [label] names it for screen readers. */
@Composable
public fun AddSwatch(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val outline = OtusColors.bgLight
    Box(
        modifier
            .size(ThemeTokens.swatchTargetWidth, PanelTokens.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, CircleShape)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(ThemeTokens.swatchSize).drawBehind {
                val width = PanelTokens.borderWidth.toPx()
                drawCircle(
                    outline,
                    radius = (size.minDimension - width) / 2,
                    style = Stroke(width, pathEffect = outlineDash()),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_add, size = ThemeTokens.swatchAddIcon)
            }
        }
    }
}

/** The dash pattern of an outline around an "add" control, such as [AddSwatch]. */
internal fun outlineDash(): PathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH, DASH))

private const val DASH = 6f
