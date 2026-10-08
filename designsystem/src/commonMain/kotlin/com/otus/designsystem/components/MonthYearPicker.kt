package com.otus.designsystem.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.theme.CalendarTokens
import kotlinx.datetime.LocalDate

/** Calendar navigation adapter with optional day selection. */
@Composable
public fun MonthYearPicker(
    initialYear: Int,
    initialMonth: Int,
    maxDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int?) -> Unit,
    initialDay: Int? = null,
) {
    SpecificDatePicker(
        initialYear,
        initialMonth,
        LocalDate(CalendarTokens.MIN_YEAR, 1, 1)..maxDate,
        onDismiss,
        onConfirm,
        initialDay = initialDay,
        dayRequired = false,
    )
}
