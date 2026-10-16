package com.otus.settings.presentation.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otus.dateformat.DateFormatPreference
import com.otus.dateformat.currentDateLocaleIdentifier
import com.otus.dateformat.currentLocalDate
import com.otus.designsystem.components.ChoiceOptions
import com.otus.designsystem.components.ChoiceRow
import com.otus.designsystem.components.ChoiceSheet
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.components.SettingsSection
import com.otus.designsystem.components.Subpage
import com.otus.designsystem.elements.ValueButton
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_calendar_month
import com.otus.designsystem.resources.symbol_palette
import com.otus.settings.presentation.DateFormatSettingsState
import com.otus.settings.presentation.ThemeSettingsState
import com.otus.settings.presentation.dateFormatExample
import com.otus.settings.presentation.themeTitle

/**
 * Shared appearance settings. The Look section opens the theme page with [onTheme] and shows the
 * date format; an app adds its own rows to it in [look], such as the sidebar, and its own sections,
 * such as gestures, below it in [content].
 */
@Composable
public fun AppearanceSettingsScreen(
    dateFormat: DateFormatSettingsState,
    theme: ThemeSettingsState,
    onTheme: () -> Unit,
    onBack: () -> Unit,
    look: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val locale = currentDateLocaleIdentifier()
    val today = remember { currentLocalDate() }
    var choosing by rememberSaveable { mutableStateOf(false) }
    Subpage("Appearance & behavior", onBack) {
        SettingsSection("Look") {
            SettingRow(
                "Theme",
                onTheme,
                description = "Colors across the whole app",
                icon = Res.drawable.symbol_palette,
            ) { ValueButton(theme.choice.themeTitle, swatch = theme.choice.accent) }
            look()
            SettingRow(
                "Date format",
                { choosing = true },
                description = "Shown as ${dateFormatExample(dateFormat.preference, locale, today)}",
                icon = Res.drawable.symbol_calendar_month,
            ) { ValueButton(dateFormat.preference.shortLabel) }
        }
        content()
    }
    if (choosing) {
        ChoiceSheet(
            title = "Date format",
            choices =
                ChoiceOptions(
                    DateFormatPreference.entries,
                    selected = dateFormat.preference,
                    default = DateFormatPreference.DeviceLocale,
                ),
            row = { ChoiceRow(it.patternLabel, dateFormatExample(it, locale, today)) },
            onSelect = dateFormat::select,
            onDismiss = { choosing = false },
        )
    }
}

/** The pattern as its value button shows it; the device setting is shortened so it fits. */
private val DateFormatPreference.shortLabel: String
    get() = if (this == DateFormatPreference.DeviceLocale) "Device" else patternLabel
