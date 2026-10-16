package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.ThemeTokens
import com.otus.designsystem.theme.contrast

/**
 * The current value of a setting in an outlined chip, such as a choice or the word "Edit". A
 * [swatch] puts a dot of that color before the value, such as the current accent. A [fill] colors
 * the whole chip, such as a color shown by its hex code, and the text takes whichever of `text1`
 * and `bgDark` reads better on it. It only shows [value]; the enclosing row acts and supplies the
 * accessible name.
 */
@Composable
public fun ValueButton(
    value: String,
    modifier: Modifier = Modifier,
    swatch: Color? = null,
    fill: Color? = null,
) {
    val shape = RoundedCornerShape(SettingTokens.valueRadius)
    Row(
        modifier
            .widthIn(max = SettingTokens.valueMaxWidth)
            .then(if (fill == null) Modifier else Modifier.background(fill, shape))
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .padding(
                start = if (swatch == null) SettingTokens.valuePaddingHorizontal else SettingTokens.valuePaddingSwatch,
                end = SettingTokens.valuePaddingHorizontal,
                top = SettingTokens.valuePaddingVertical,
                bottom = SettingTokens.valuePaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.valueSwatchGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatch?.let {
            Box(
                Modifier
                    .size(ThemeTokens.valueDot)
                    .background(it, CircleShape)
                    .border(PanelTokens.borderWidth, OtusColors.accentLight, CircleShape),
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

private fun readableOn(fill: Color): Color = listOf(OtusColors.text1, OtusColors.bgDark).maxBy { contrast(it, fill) }
