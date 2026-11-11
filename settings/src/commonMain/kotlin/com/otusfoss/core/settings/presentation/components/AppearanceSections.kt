package com.otusfoss.core.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otusfoss.core.dateformat.DateFormatPreference
import com.otusfoss.core.dateformat.currentDateLocaleIdentifier
import com.otusfoss.core.dateformat.currentLocalDate
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_calendar_month
import com.otusfoss.core.designsystem.resources.symbol_palette
import com.otusfoss.core.designsystem.resources.symbol_speed
import com.otusfoss.core.designsystem.shared.selection.components.ChoiceOptions
import com.otusfoss.core.designsystem.shared.selection.components.ChoiceRow
import com.otusfoss.core.designsystem.shared.selection.components.ChoiceSheet
import com.otusfoss.core.designsystem.theme.motion.AnimationSpeed
import com.otusfoss.core.settings.presentation.AnimationSettingsState
import com.otusfoss.core.settings.presentation.DateFormatSettingsState
import com.otusfoss.core.settings.presentation.ThemeSettingsState
import com.otusfoss.core.settings.presentation.dateFormatExample
import com.otusfoss.core.settings.presentation.options
import com.otusfoss.core.settings.presentation.themeTitle
import kotlinx.datetime.LocalDate

/**
 * The shared Appearance page: its Look section opens the Theme page with [openTheme] and holds the
 * date format and animation speed, with the app's own [look] rows, such as the mail list, after
 * Theme. The page shows these with [SettingSections] and search lists them with [sections].
 */
public fun appearanceSections(
    dateFormat: DateFormatSettingsState,
    theme: ThemeSettingsState,
    animation: AnimationSettingsState,
    openTheme: (setting: String?) -> Unit,
    look: List<SettingRowSpec> = emptyList(),
): List<SettingsSectionSpec> {
    val locale = currentDateLocaleIdentifier()
    val today = currentLocalDate()
    return listOf(
        SettingsSectionSpec(
            "Look",
            buildList {
                add(
                    PageRowSpec(
                        "Theme",
                        openTheme,
                        value = theme.choice.themeTitle,
                        icon = Res.drawable.symbol_palette,
                        keywords =
                            listOf("Dark mode", "Light mode", "Accent color", "Colors") +
                                theme.choice.options.map { it.title },
                    ),
                )
                addAll(look)
                add(
                    ChoiceRowSpec(
                        "Date format",
                        dateFormat.preference.shortLabel,
                        { DateFormatSheet(dateFormat, locale, today, it) },
                        DateFormatPreference.entries.map { it.patternLabel },
                        Res.drawable.symbol_calendar_month,
                        description = "Shown as ${dateFormatExample(dateFormat.preference, locale, today)}",
                        keywords = listOf("Dates"),
                    ),
                )
                add(
                    ChoiceRowSpec(
                        "Animations",
                        animation.speed.label,
                        { AnimationSpeedSheet(animation, it) },
                        AnimationSpeed.entries.map { it.label },
                        Res.drawable.symbol_speed,
                        keywords = listOf("Motion", "Reduce motion", "Transitions"),
                    ),
                )
            },
        ),
    )
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

private val AnimationSpeed.label: String
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
private val DateFormatPreference.shortLabel: String
    get() = if (this == DateFormatPreference.DeviceLocale) "Device" else patternLabel
