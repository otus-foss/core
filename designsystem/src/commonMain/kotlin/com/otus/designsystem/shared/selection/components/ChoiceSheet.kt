package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.LocalIconTint
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.folders.elements.FolderColorSwatch
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.people.components.NamedAvatar
import com.otus.designsystem.shared.people.components.PersonText
import com.otus.designsystem.shared.rows.components.topHairline
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.people.SummaryTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A bottom sheet that picks one of [choices], in rounded groups with a check beside the selected
 * one. Choosing an option applies it at once and moves the check; Done, the scrim, Back, or
 * dragging the sheet down closes it. [row] says how each option reads, and [footnote] explains the
 * choice in `text2` below the groups.
 */
@Composable
public fun <T> ChoiceSheet(
    title: String,
    choices: ChoiceOptions<T>,
    row: (T) -> ChoiceRow,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
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
                ChoiceGroup(group, choices.selected, row, onSelect)
            }
            if (footnote != null) {
                Text(
                    footnote,
                    Modifier.padding(horizontal = ChoiceSheetTokens.optionPadding),
                    color = OtusColors.text2,
                    fontSize = SheetTokens.detailSize,
                )
            }
        }
    }
}

/**
 * How one [ChoiceSheet] option reads: [label] over an optional `text2` [detail], after [icon]. A
 * [person] named in the label gets their avatar before the name, as in [PersonText]. With a
 * [keyword], the label is a [ValueToken] instead, such as "From" before an address. [iconTint]
 * colors the icon, such as a folder's color. A [swatch] shows a folder color before the label;
 * screen readers hear only the label.
 */
public data class ChoiceRow(
    val label: String,
    val detail: String? = null,
    val icon: DrawableResource? = null,
    val person: NamedAvatar? = null,
    val keyword: String? = null,
    val iconTint: Color? = null,
    val swatch: FolderColor? = null,
)

@Composable
private fun ChoiceOption(
    row: ChoiceRow,
    selected: Boolean,
    first: Boolean,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = ChoiceSheetTokens.optionMinHeight)
            .choiceDivider(first)
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, RoundedCornerShape(0))
            .padding(horizontal = ChoiceSheetTokens.optionPadding, vertical = ChoiceSheetTokens.optionGap),
        horizontalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.optionGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row.icon != null) {
            CompositionLocalProvider(LocalIconTint provides row.iconTint) { LeadingSymbol(row.icon) }
        }
        if (row.swatch != null) FolderColorSwatch(row.swatch)
        val lineGap = if (row.keyword != null) SummaryTokens.avatarGap else SettingTokens.textGap
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(lineGap)) {
            if (row.keyword != null) {
                ValueToken(row.label, keyword = row.keyword, avatar = row.person?.avatar)
            } else {
                PersonText(
                    row.label,
                    row.person,
                    fontSize = SettingTokens.titleSize,
                    lineHeight = SettingTokens.titleLineHeight,
                )
            }
            if (row.detail != null) {
                Text(
                    row.detail,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        // Every row keeps room for the check, so moving it never rewraps a long label.
        Box(Modifier.size(PanelTokens.iconSize)) { if (selected) Symbol(Res.drawable.symbol_check) }
    }
}

@Composable
private fun <T> ChoiceGroup(
    options: List<T>,
    selected: T,
    row: (T) -> ChoiceRow,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.clip(SmoothCornerShape(SheetTokens.rowRadius)).background(OtusColors.bgRegular)) {
        options.forEachIndexed { index, option ->
            ChoiceOption(row(option), option == selected, first = index == 0) { onSelect(option) }
        }
    }
}

private fun Modifier.choiceDivider(first: Boolean): Modifier = if (first) this else topHairline(OtusColors.strokeBgDark)
