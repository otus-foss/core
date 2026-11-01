package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens

/**
 * One option in a grid of pictures, such as the app icons: a big [icon] with [label] below. The
 * selected one sits on `selectedFill`; the whole card is one radio button.
 */
@Composable
public fun IconChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val shape = SmoothCornerShape(ChoiceSheetTokens.iconChoiceRadius)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .background(if (selected) OtusColors.selectedFill else Color.Transparent, shape)
            .padding(ChoiceSheetTokens.iconChoicePadding),
        verticalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.iconChoiceGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Text(
            label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = SettingTokens.descriptionSize,
            lineHeight = SettingTokens.descriptionLineHeight,
        )
    }
}
