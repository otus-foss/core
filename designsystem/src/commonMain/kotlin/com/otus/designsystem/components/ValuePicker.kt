package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [ValueToken] the user taps to change its [value]: the token, a thin divider, and a down
 * chevron in one outline. The whole picker is one 48 dp tall target that calls [onClick], where
 * the app opens its choices, for example a folder sheet. Screen readers hear "[keyword] [value],
 * change". A disabled picker dims and ignores taps.
 */
@Composable
public fun ValuePicker(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyword: String? = null,
    avatar: ChipAvatar? = null,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
) {
    val interactions = remember { MutableInteractionSource() }
    val label = valuePickerLabel(value, keyword)
    Box(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides if (enabled) OtusColors.text1 else OtusColors.text3) {
            ValueOutline(Modifier.controlPressFeedback(interactions, RoundedCornerShape(SettingTokens.valueRadius))) {
                ValueLabel(value, keyword, avatar, icon, enabled)
                Box(Modifier.size(PanelTokens.borderWidth, PanelTokens.iconSize).background(OtusColors.bgLight))
                Symbol(Res.drawable.symbol_expand_more)
            }
        }
    }
}

/** What a screen reader says for a [ValuePicker], such as "Move to Receipts, change". */
internal fun valuePickerLabel(
    value: String,
    keyword: String?,
): String = listOfNotNull(keyword, value).joinToString(" ") + ", change"
