package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.theme.CalendarTokens
import com.otus.designsystem.theme.SheetTokens
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * The date wheels for a birthday or another yearly date: day, month, and a year that may stay
 * "No year". The years run from this year back 120 years, widened to include [initial]'s year.
 * Done confirms; [onRemove], when given, clears the date.
 */
@Composable
public fun BirthdayPicker(
    initial: YearlyDate?,
    onDismiss: () -> Unit,
    onConfirm: (YearlyDate) -> Unit,
    title: String = "Birthday",
    onRemove: (() -> Unit)? = null,
) {
    val thisYear = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).year }
    // A stored year outside the usual range stays on the wheel, so Done keeps it.
    val latest = maxOf(thisYear, initial?.year ?: thisYear)
    val earliest = minOf(thisYear - YEARS_BACK, initial?.year ?: thisYear)
    val years = remember(latest, earliest) { listOf(NO_YEAR) + (latest downTo earliest).map(Int::toString) }
    var month by rememberSaveable { mutableIntStateOf(initial?.month ?: 1) }
    var day by rememberSaveable { mutableIntStateOf(initial?.day ?: 1) }
    var year by rememberSaveable { mutableStateOf(initial?.year) }
    // Without a year, February keeps its 29th, since the date may fall in a leap year.
    val days = 1..daysIn(year ?: LEAP_YEAR, month)
    val shownDay = day.coerceIn(days)
    DatePickerDialog(onDismiss) {
        DatePickerHeading(title, "Leave the year as No year when you don’t know it.", onDismiss)
        DateWheels(Modifier.weight(1f)) {
            DatePickerColumn("Day", days.map(Int::toString), shownDay - 1, { day = it + 1 }, Modifier.weight(1f))
            DatePickerColumn("Month", MONTH_NAMES, month - 1, {
                month = it + 1
            }, Modifier.weight(CalendarTokens.MONTH_COLUMN_WEIGHT))
            DatePickerColumn(
                "Year",
                years,
                year?.let { latest - it + 1 } ?: 0,
                { year = if (it == 0) null else latest - it + 1 },
                Modifier.weight(CalendarTokens.YEAR_COLUMN_WEIGHT),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap)) {
            onRemove?.let { ActionButton("Remove", it, Modifier.weight(1f)) }
            ActionButton("Done", { onConfirm(YearlyDate(month, shownDay, year)) }, Modifier.weight(1f), primary = true)
        }
    }
}

private fun daysIn(
    year: Int,
    month: Int,
): Int {
    val first = LocalDate(year, month, 1)
    val next = if (month == MONTHS) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    return next.toEpochDays().toInt() - first.toEpochDays().toInt()
}

private const val NO_YEAR = "No year"
private const val YEARS_BACK = 120
private const val LEAP_YEAR = 2000
private const val MONTHS = 12
