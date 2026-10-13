package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check_circle
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ThemeTokens

/**
 * A theme to pick: a small sketch of a page drawn in [sketch]'s colors above [title] and
 * [description]. The selected card has an accent ring and a check; the card is one radio button.
 */
@Composable
public fun ThemeCard(
    title: String,
    description: String,
    sketch: OtusPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ThemeTokens.cardRadius)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .background(OtusColors.bgDark, shape)
            .border(
                if (selected) PanelTokens.borderWidth * 2 else PanelTokens.borderWidth,
                if (selected) OtusColors.accentLight else OtusColors.strokeBgDark,
                shape,
            ).padding(ThemeTokens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
    ) {
        ThemeSketch(sketch)
        Row(
            Modifier.padding(horizontal = PanelTokens.iconGap).padding(bottom = PanelTokens.iconGap),
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = ThemeTokens.cardTitleSize,
                    lineHeight = ThemeTokens.cardTitleLineHeight,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    description,
                    color = OtusColors.text2,
                    fontSize = ThemeTokens.cardDetailSize,
                    lineHeight = ThemeTokens.cardDetailLineHeight,
                )
            }
            if (selected) {
                CompositionLocalProvider(LocalContentColor provides OtusColors.accentLight) {
                    Symbol(Res.drawable.symbol_check_circle)
                }
            }
        }
    }
}

/** A page with a bar and two lines of text, the second led by the accent. */
@Composable
private fun ThemeSketch(sketch: OtusPalette) {
    val shape = RoundedCornerShape(ThemeTokens.sketchRadius)
    Column(
        Modifier
            .fillMaxWidth()
            .height(ThemeTokens.sketchHeight)
            .background(sketch.bgRegular, shape)
            .border(PanelTokens.borderWidth, sketch.strokeBgDark, shape)
            .padding(ThemeTokens.sketchPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.sketchGap),
    ) {
        val bar = RoundedCornerShape(ThemeTokens.sketchBarRadius)
        Box(
            Modifier
                .fillMaxWidth()
                .height(ThemeTokens.sketchBarHeight)
                .background(sketch.bgDark, bar)
                .border(PanelTokens.borderWidth, sketch.strokeBgDark, bar),
        )
        SketchLine(sketch.bgLight, sketch.text2, ThemeTokens.SKETCH_LONG_LINE)
        SketchLine(sketch.accentLight, sketch.text1, ThemeTokens.SKETCH_SHORT_LINE)
    }
}

@Composable
private fun SketchLine(
    dot: Color,
    line: Color,
    length: Float,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.sketchGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ThemeTokens.sketchDot).background(dot, CircleShape))
        Box(Modifier.fillMaxWidth(length).height(ThemeTokens.sketchLineHeight).background(line, CircleShape))
    }
}
