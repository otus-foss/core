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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_calendar_month
import com.otus.designsystem.resources.symbol_palette
import com.otus.designsystem.resources.symbol_speed
import com.otus.designsystem.shared.pages.components.Subpage
import com.otus.designsystem.shared.selection.components.ChoiceOptions
import com.otus.designsystem.shared.selection.components.ChoiceRow
import com.otus.designsystem.shared.selection.components.ChoiceSheet
import com.otus.designsystem.shared.settings.components.SettingPageRow
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.components.SettingsSection
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.designsystem.theme.motion.AnimationSpeed
import com.otus.settings.presentation.AnimationSettingsState
import com.otus.settings.presentation.DateFormatSettingsState
import com.otus.settings.presentation.ThemeSettingsState
import com.otus.settings.presentation.dateFormatExample
import com.otus.settings.presentation.themeTitle
import kotlinx.datetime.LocalDate

/**
 * Shared appearance settings. The Look section opens the theme page with [onTheme], named by the
 * current theme, and shows the date format and animation speed; an app adds its own rows to it in
 * [look], such as the mail list. Behavior, such as gestures or the sidebar, belongs on a page of
 * the app's own.
 */
@Composable
public fun AppearanceSettingsScreen(
    dateFormat: DateFormatSettingsState,
    theme: ThemeSettingsState,
    animation: AnimationSettingsState,
    onTheme: () -> Unit,
    onBack: () -> Unit,
    look: @Composable ColumnScope.() -> Unit = {},
) {
    val locale = currentDateLocaleIdentifier()
    val today = remember { currentLocalDate() }
    var choosingDateFormat by rememberSaveable { mutableStateOf(false) }
    var choosingSpeed by rememberSaveable { mutableStateOf(false) }
    Subpage("Appearance", onBack) {
        SettingsSection("Look") {
            SettingPageRow(
                "Theme",
                onTheme,
                description = theme.choice.themeTitle,
                icon = Res.drawable.symbol_palette,
            )
            look()
            SettingRow(
                "Date format",
                { choosingDateFormat = true },
                description = "Shown as ${dateFormatExample(dateFormat.preference, locale, today)}",
                icon = Res.drawable.symbol_calendar_month,
            ) { ValueButton(dateFormat.preference.shortLabel) }
            SettingRow(
                "Animations",
                { choosingSpeed = true },
                icon = Res.drawable.symbol_speed,
            ) { ValueButton(animation.speed.label) }
        }
    }
    if (choosingDateFormat) DateFormatSheet(dateFormat, locale, today) { choosingDateFormat = false }
    if (choosingSpeed) AnimationSpeedSheet(animation) { choosingSpeed = false }
}

@Composable
private fun DateFormatSheet(
    dateFormat: DateFormatSettingsState,
    locale: String,
    today: LocalDate,
    onDismiss: () -> Unit,
) {
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
        onDismiss = onDismiss,
    )
}

@Composable
private fun AnimationSpeedSheet(
    animation: AnimationSettingsState,
    onDismiss: () -> Unit,
) {
    ChoiceSheet(
        title = "Animations",
        choices = ChoiceOptions(AnimationSpeed.entries, selected = animation.speed),
        row = { ChoiceRow(it.label, it.detail) },
        onSelect = animation::select,
        onDismiss = onDismiss,
    )
}

internal val AnimationSpeed.label: String
    get() =
        when (this) {
            AnimationSpeed.Instant -> "Instant"
            AnimationSpeed.Slow -> "Slow"
            AnimationSpeed.Regular -> "Regular"
            AnimationSpeed.Fast -> "Fast"
        }

private val AnimationSpeed.detail: String?
    get() =
        when (this) {
            AnimationSpeed.Instant -> "No animations"
            AnimationSpeed.Regular -> "Default"
            else -> null
        }

/** The pattern as its value button shows it; the device setting is shortened so it fits. */
internal val DateFormatPreference.shortLabel: String
    get() = if (this == DateFormatPreference.DeviceLocale) "Device" else patternLabel
