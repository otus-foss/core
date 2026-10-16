package com.otus.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.LeadingSymbol
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SummaryTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [value], such as an address, a name, or a subject word, in an outlined box. An optional
 * [keyword] before it says what the value is, such as "From" or "Subject", and an optional
 * [avatar] shows the person or company it names, or an [icon] what kind of value it is. Long
 * values end in an ellipsis. It only shows the value; [ValuePicker] lets the user change it.
 */
@Composable
public fun ValueToken(
    value: String,
    modifier: Modifier = Modifier,
    keyword: String? = null,
    avatar: ChipAvatar? = null,
    icon: DrawableResource? = null,
) {
    ValueOutline(modifier) { ValueLabel(value, keyword, avatar, icon) }
}

/** The outlined box of a [ValueToken] or [ValuePicker]. */
@Composable
internal fun ValueOutline(
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .valueOutline(RoundedCornerShape(SettingTokens.valueRadius))
            .padding(horizontal = SettingTokens.valuePaddingHorizontal, vertical = SettingTokens.valuePaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(SummaryTokens.avatarGap),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** The `bgLight` stroke around a [ValueToken], [ValuePicker], or [RuleChip]. */
internal fun Modifier.valueOutline(shape: Shape): Modifier = border(PanelTokens.borderWidth, OtusColors.bgLight, shape)

/**
 * The keyword, avatar or icon, and value inside a [ValueOutline]. A disabled label dims its
 * keyword. A [fill] label's value takes the outline's spare width.
 */
@Composable
internal fun RowScope.ValueLabel(
    value: String,
    keyword: String?,
    avatar: ChipAvatar?,
    icon: DrawableResource?,
    enabled: Boolean = true,
    fill: Boolean = false,
) {
    keyword?.let {
        Text(
            it,
            color = if (enabled) OtusColors.text2 else OtusColors.text3,
            fontSize = SettingTokens.descriptionSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
    avatar?.let { SmallAvatar(it) }
    icon?.let { LeadingSymbol(it, SummaryTokens.avatarSize, enabled) }
    Text(
        value,
        Modifier.weight(1f, fill = fill),
        fontSize = SettingTokens.titleSize,
        lineHeight = SettingTokens.titleLineHeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
