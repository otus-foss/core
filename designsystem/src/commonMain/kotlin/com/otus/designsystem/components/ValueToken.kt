package com.otus.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SummaryTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [value], such as an address, a name, or a subject word, in an outlined box. An optional
 * [keyword] before it says what the value is, such as "From" or "Subject", and an optional
 * [avatar] shows the person or company it names, or an [icon] what kind of value it is. Long
 * values end in an ellipsis. With [onClick] it is a picker for the value: it fills the width it
 * is given and ends in a down chevron.
 */
@Composable
public fun ValueToken(
    value: String,
    modifier: Modifier = Modifier,
    keyword: String? = null,
    avatar: ChipAvatar? = null,
    icon: DrawableResource? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .then(onClick?.let { Modifier.picker(it) } ?: Modifier)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, RoundedCornerShape(SettingTokens.valueRadius))
            .padding(horizontal = SettingTokens.valuePaddingHorizontal, vertical = SettingTokens.valuePaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(SummaryTokens.avatarGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keyword?.let {
            Text(
                it,
                color = OtusColors.text2,
                fontSize = SettingTokens.descriptionSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        avatar?.let { SmallAvatar(it) }
        icon?.let { Symbol(it, size = SummaryTokens.avatarSize) }
        Text(
            value,
            Modifier.weight(1f, fill = onClick != null),
            fontSize = SettingTokens.titleSize,
            lineHeight = SettingTokens.titleLineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onClick != null) Symbol(Res.drawable.symbol_expand_more)
    }
}

@Composable
private fun Modifier.picker(onClick: () -> Unit): Modifier {
    val interactions = remember { MutableInteractionSource() }
    return fillMaxWidth()
        .heightIn(min = PanelTokens.touchTarget)
        .clickable(interactions, indication = null, role = Role.DropdownList, onClick = onClick)
        .controlPressFeedback(interactions, RoundedCornerShape(SettingTokens.valueRadius))
}
