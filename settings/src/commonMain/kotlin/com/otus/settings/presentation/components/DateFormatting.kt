package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.otus.dateformat.DateFormatter
import com.otus.dateformat.currentDateLocaleIdentifier
import com.otus.settings.presentation.DateFormatSettingsState

/**
 * The formatter for the user's date format and the device locale. It is built again only when
 * either changes.
 */
@Composable
public fun rememberDateFormatter(settings: DateFormatSettingsState): DateFormatter {
    val localeIdentifier = currentDateLocaleIdentifier()
    return remember(settings.preference, localeIdentifier) { DateFormatter(settings.preference, localeIdentifier) }
}

/** The formatter of the user's date format, which the app provides around its pages. */
public val LocalDateFormatter: ProvidableCompositionLocal<DateFormatter> = staticCompositionLocalOf { DateFormatter() }
