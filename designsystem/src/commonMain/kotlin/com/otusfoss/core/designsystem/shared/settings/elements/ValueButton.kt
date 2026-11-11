package com.otusfoss.core.designsystem.shared.settings.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.theme.colors.LocalOnSelectedFill
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.readableOn
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.ThemeTokens

/**
 * The current value of a setting in an outlined chip, such as a choice or the word "Edit". A
 * [swatch] puts a dot of that color before the value, such as the current accent. A [fill] colors
 * the whole chip, such as a color shown by its hex code, and the text takes whichever of `text1`
 * and `bgDark` reads better on it. [outline] strokes it, such as `text3` on a `selectedFill` row.
 * It only shows [value]; the enclosing row acts and supplies the accessible name.
 */
@Composable
public fun ValueButton(
    value: String,
    modifier: Modifier = Modifier,
    swatch: Color? = null,
    fill: Color? = null,
    outline: Color = if (LocalOnSelectedFill.current) OtusColors.text3 else OtusColors.bgLight,
) {
    val shape = SmoothCornerShape(SettingTokens.valueRadius)
    Row(
        modifier
            .widthIn(max = SettingTokens.valueMaxWidth)
            .valueBackground(fill, shape)
            .strokeBorder(Sizes.borderWidth, outline, shape)
            .padding(
                start = if (swatch == null) SettingTokens.valuePaddingHorizontal else SettingTokens.valuePaddingSwatch,
                end = SettingTokens.valuePaddingHorizontal,
                top = SettingTokens.valuePaddingVertical,
                bottom = SettingTokens.valuePaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.valueSwatchGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (swatch != null) {
            Box(
                Modifier
                    .size(ThemeTokens.valueDot)
                    .background(swatch, CircleShape)
                    .border(Sizes.borderWidth, OtusColors.text2, CircleShape),
            )
        }
        Text(
            value,
            Modifier.weight(1f, fill = false),
            color = fill?.let(::readableOn) ?: Color.Unspecified,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.valueBackground(
    fill: Color?,
    shape: Shape,
): Modifier = fill?.let { background(it, shape) } ?: this
