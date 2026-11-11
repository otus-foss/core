package com.otusfoss.core.designsystem.shared.dates.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.components.ButtonPair
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.theme.tokens.selection.CalendarTokens
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
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
    val days = 1..YearMonth(year ?: LEAP_YEAR, month).numberOfDays
    val shownDay = day.coerceIn(days)
    DatePickerDialog(onDismiss) {
        DatePickerHeading(title, null, onDismiss)
        DateWheels(Modifier.weight(1f)) {
            DatePickerColumn("Day", days.map(Int::toString), shownDay - 1, { day = it + 1 }, Modifier.weight(1f))
            DatePickerColumn(
                "Month",
                MONTH_NAMES,
                month - 1,
                { month = it + 1 },
                Modifier.weight(CalendarTokens.MONTH_COLUMN_WEIGHT),
            )
            DatePickerColumn(
                "Year",
                years,
                year?.let { latest - it + 1 } ?: 0,
                { year = if (it == 0) null else latest - it + 1 },
                Modifier.weight(CalendarTokens.YEAR_COLUMN_WEIGHT),
            )
        }
        val done = { onConfirm(YearlyDate(month, shownDay, year)) }
        if (onRemove == null) {
            ActionButton("Done", done, Modifier.fillMaxWidth(), primary = true)
        } else {
            ButtonPair(UiAction("Remove", onClick = onRemove), UiAction("Done", onClick = done))
        }
    }
}

private const val NO_YEAR = "No year"
private const val YEARS_BACK = 120
private const val LEAP_YEAR = 2000
