package com.otus.designsystem.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Wheel picker for one date within [allowedDates]. Go stays disabled until a day is chosen.
 * Future dates are allowed when [allowedDates] includes them.
 */
@Composable
public fun SpecificDatePicker(
    initialMonth: YearMonth,
    allowedDates: ClosedRange<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    initialDay: Int? = null,
) {
    DateWheelPicker(initialMonth, initialDay, allowedDates, dayRequired = true, onDismiss) { _, date ->
        date?.let(onConfirm)
    }
}
