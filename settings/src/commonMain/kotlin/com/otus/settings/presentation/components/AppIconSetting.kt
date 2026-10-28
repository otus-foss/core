package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_owl
import com.otus.designsystem.shared.selection.components.ChoiceOptions
import com.otus.designsystem.shared.selection.components.ChoiceRow
import com.otus.designsystem.shared.selection.components.ChoiceSheet
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.designsystem.theme.brand.AppIconStyle
import com.otus.settings.presentation.AppIconSettingsState

/**
 * The App icon row and its choice sheet, for an app that can switch its launcher icon. It goes in
 * the appearance page's Look section.
 */
@Composable
public fun AppIconSetting(appIcon: AppIconSettingsState) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    SettingRow(
        "App icon",
        { choosing = true },
        description = "Also colors the launch animation",
        icon = Res.drawable.symbol_owl,
    ) { ValueButton(appIcon.style.label) }
    if (choosing) {
        ChoiceSheet(
            title = "App icon",
            choices = ChoiceOptions(AppIconStyle.entries, selected = appIcon.style, default = AppIconStyle.Accent),
            row = { ChoiceRow(it.label, it.detail) },
            onSelect = appIcon::select,
            onDismiss = { choosing = false },
        )
    }
}

private val AppIconStyle.label: String
    get() =
        when (this) {
            AppIconStyle.Accent -> "Accent"
            AppIconStyle.Monochrome -> "Monochrome"
        }

private val AppIconStyle.detail: String
    get() =
        when (this) {
            AppIconStyle.Accent -> "The owl in your accent color"
            AppIconStyle.Monochrome -> "The owl in the text color"
        }
