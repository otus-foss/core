package com.otus.designsystem.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens

/**
 * The current value of a choice, outlined with a down chevron. It only shows [value]; the
 * enclosing row opens the choice and supplies the accessible name. A [wide] button fits longer
 * values, such as an email address, beside a short title.
 */
@Composable
public fun ValueButton(
    value: String,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    Row(
        modifier
            .widthIn(max = if (wide) SettingTokens.wideValueMaxWidth else SettingTokens.valueMaxWidth)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, RoundedCornerShape(SettingTokens.valueRadius))
            .padding(horizontal = SettingTokens.valuePaddingHorizontal, vertical = SettingTokens.valuePaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(value, Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Symbol(Res.drawable.symbol_expand_more)
    }
}
