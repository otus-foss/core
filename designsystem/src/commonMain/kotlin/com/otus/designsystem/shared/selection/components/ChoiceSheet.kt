package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.LocalIconTint
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.folders.elements.FolderColorSwatch
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.overlays.elements.sheetBodyInset
import com.otus.designsystem.shared.people.components.NamedAvatar
import com.otus.designsystem.shared.people.components.PersonText
import com.otus.designsystem.shared.rows.components.topHairline
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.people.SummaryTokens
import com.otus.designsystem.theme.tokens.selection.PickTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A bottom sheet that picks one of [choices], in rounded groups with a check beside the selected
 * one. Choosing an option applies it through [onSelect] at once and moves the check; Done, the
 * scrim, Back, or dragging the sheet down closes it. With [saveOnDone], choosing only moves the
 * check, and Done applies the picked option. [row] says how each option reads, and [footnote]
 * explains the choice in `text2` below the groups.
 */
@Composable
public fun <T> ChoiceSheet(
    title: String,
    choices: ChoiceOptions<T>,
    row: (T) -> ChoiceRow,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    footnote: String? = null,
    saveOnDone: SaveOnDone<T>? = null,
) {
    // The index survives process death for any option type; the option itself might not be saveable.
    var pickedIndex by rememberSaveable { mutableIntStateOf(choices.options.indexOf(choices.selected)) }
    val picked =
        if (saveOnDone ==
            null
        ) {
            choices.selected
        } else {
            choices.options.getOrElse(pickedIndex) { choices.selected }
        }
    val done =
        if (saveOnDone == null) {
            UiAction("Done", onClick = onDismiss)
        } else {
            UiAction(
                "Done",
                enabled = saveOnDone.enabled(picked),
                onClick = { saveAndClose(picked, onSelect, onDismiss) },
            )
        }
    SheetFrame(title, onDismiss, done = done) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .sheetBodyInset()
                // One radio group across the default's group and the rest.
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.groupGap),
        ) {
            choices.groups.forEach { group ->
                ChoiceGroup(group, picked, row) { option ->
                    if (saveOnDone == null) onSelect(option) else pickedIndex = choices.options.indexOf(option)
                }
            }
            saveOnDone?.below?.invoke(this, picked)
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
 * A [ChoiceSheet] that applies on Done: Done is [enabled] for the picked option, and [below] shows
 * under the groups for it, such as a field the option needs.
 */
public class SaveOnDone<T>(
    public val enabled: (T) -> Boolean = { true },
    public val below: @Composable ColumnScope.(T) -> Unit = {},
)

private fun <T> saveAndClose(
    picked: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    onSelect(picked)
    onDismiss()
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
    PickRowFrame(
        PickMark(selected, style = PickStyle.Check),
        onClick,
        Modifier.heightIn(min = PickTokens.optionHeight).choiceDivider(first),
        leading =
            if (row.icon != null || row.swatch != null) {
                { ChoiceLeading(row) }
            } else {
                null
            },
    ) { ChoiceText(row) }
}

@Composable
private fun ChoiceLeading(row: ChoiceRow) {
    Row(horizontalArrangement = Arrangement.spacedBy(ChoiceSheetTokens.optionGap)) {
        if (row.icon != null) {
            CompositionLocalProvider(LocalIconTint provides row.iconTint) { LeadingSymbol(row.icon) }
        }
        if (row.swatch != null) FolderColorSwatch(row.swatch)
    }
}

@Composable
private fun ChoiceText(row: ChoiceRow) {
    val lineGap = if (row.keyword != null) SummaryTokens.avatarGap else SettingTokens.textGap
    Column(verticalArrangement = Arrangement.spacedBy(lineGap)) {
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
