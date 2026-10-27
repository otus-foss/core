package com.otus.designsystem.shared.dates.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Wheel picker for a month within [allowedDates], with an optional day.
 * [onConfirm] receives a null date when the day is left as Any.
 */
@Composable
public fun MonthYearPicker(
    initialMonth: YearMonth,
    allowedDates: ClosedRange<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth, LocalDate?) -> Unit,
    initialDay: Int? = null,
) {
    DateWheelPicker(initialMonth, initialDay, allowedDates, dayRequired = false, onDismiss, onConfirm)
}
