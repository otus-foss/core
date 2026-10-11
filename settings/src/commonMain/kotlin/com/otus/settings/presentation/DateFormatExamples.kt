package com.otus.settings.presentation

import com.otus.dateformat.DateFormatPreference
import com.otus.dateformat.DateFormatter
import kotlinx.datetime.LocalDate

public fun dateFormatExample(
    preference: DateFormatPreference,
    localeIdentifier: String,
    today: LocalDate,
): String = DateFormatter(preference, localeIdentifier).format(today)
