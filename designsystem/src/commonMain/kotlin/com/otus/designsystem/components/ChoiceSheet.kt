package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.theme.ChoiceSheetTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens

/**
 * A bottom sheet that picks one of [choices], in rounded groups with a check beside the selected
 * one. Choosing an option applies it at once and moves the check; Done, the scrim, Back, or
 * dragging the sheet down closes it. [footnote] explains the choice in `text2` below the groups.
 */
@Composable
public fun <T> ChoiceSheet(
    title: String,
    choices: ChoiceOptions<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    detail: (T) -> String? = { null },
    footnote: String? = null,
) {
    SheetFrame(title, onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset)
                // One radio group across the default's group and the rest.
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.groupGap),
        ) {
            choices.groups.forEach { group ->
                Column(Modifier.clip(RoundedCornerShape(SheetTokens.rowRadius)).background(OtusColors.bgRegular)) {
                    group.forEachIndexed { index, option ->
                        ChoiceOption(label(option), detail(option), option == choices.selected, first = index == 0) {
                            onSelect(option)
                        }
                    }
                }
            }
            footnote?.let {
                Text(
                    it,
                    Modifier.padding(horizontal = ChoiceSheetTokens.optionPadding),
                    color = OtusColors.text2,
                    fontSize = SheetTokens.detailSize,
                )
            }
        }
    }
}

@Composable
private fun ChoiceOption(
    label: String,
    detail: String?,
    selected: Boolean,
    first: Boolean,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = ChoiceSheetTokens.optionMinHeight)
            .then(if (first) Modifier else Modifier.topHairline(OtusColors.strokeBgDark))
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, RoundedCornerShape(0))
            .padding(horizontal = ChoiceSheetTokens.optionPadding, vertical = ChoiceSheetTokens.optionGap),
        horizontalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.optionGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingTokens.textGap)) {
            Text(label, fontSize = SettingTokens.titleSize, lineHeight = SettingTokens.titleLineHeight)
            detail?.let {
                Text(
                    it,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        if (selected) Symbol(Res.drawable.symbol_check)
    }
}
