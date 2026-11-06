package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [ValueToken] the user taps to change its [value]: the token, a thin divider, and a down
 * chevron in one outline. The whole picker is one 48 dp tall target that calls [onClick], where
 * the app opens its choices, for example a folder sheet. Screen readers hear "[keyword] [value],
 * change". A disabled picker dims and ignores taps. It fits its content; with
 * `Modifier.fillMaxWidth()` the outline fills the width and the chevron sits at its end.
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
    BoxWithConstraints(
        modifier
            .heightIn(min = Sizes.touchTarget)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        // A caller that fixes the width, such as with fillMaxWidth, stretches the outline to it.
        val fill = constraints.hasBoundedWidth && constraints.minWidth == constraints.maxWidth
        CompositionLocalProvider(LocalContentColor provides if (enabled) OtusColors.text1 else OtusColors.text3) {
            ValueOutline(
                Modifier
                    .fillPickerWidth(fill)
                    .controlPressFeedback(interactions, SmoothCornerShape(SettingTokens.valueRadius)),
            ) {
                ValueLabel(value, keyword, avatar, icon, enabled, fill)
                Box(Modifier.size(Sizes.borderWidth, Sizes.iconSize).background(OtusColors.bgLight))
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

private fun Modifier.fillPickerWidth(fill: Boolean): Modifier = if (fill) fillMaxWidth() else this
