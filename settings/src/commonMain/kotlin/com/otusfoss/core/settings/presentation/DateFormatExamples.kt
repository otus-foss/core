package com.otusfoss.core.settings.presentation

import com.otusfoss.core.dateformat.DateFormatPreference
import com.otusfoss.core.dateformat.DateFormatter
import kotlinx.datetime.LocalDate

public fun dateFormatExample(
    preference: DateFormatPreference,
    localeIdentifier: String,
    today: LocalDate,
): String = DateFormatter(preference, localeIdentifier).format(today)
