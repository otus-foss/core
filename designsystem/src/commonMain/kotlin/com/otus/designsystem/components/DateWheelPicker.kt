package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.WheelPicker
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.CalendarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.onDay

/** Shared wheel body. [dayRequired] keeps Go disabled while the day is unselected. */
@Composable
internal fun DateWheelPicker(
    initialMonth: YearMonth,
    initialDay: Int?,
    allowedDates: ClosedRange<LocalDate>,
    dayRequired: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth, LocalDate?) -> Unit,
) {
    val minDate = allowedDates.start
    val maxDate = allowedDates.endInclusive
    val bounds = remember(minDate, maxDate) { DatePickerBounds(minDate, maxDate) }
    var year by rememberSaveable { mutableIntStateOf(initialMonth.year.coerceIn(minDate.year, maxDate.year)) }
    var month by rememberSaveable { mutableIntStateOf(initialMonth.month.number) }
    var day by rememberSaveable { mutableStateOf(initialDay) }
    val selectedYear = year.coerceIn(minDate.year, maxDate.year)
    val months = bounds.months(selectedYear)
    val selectedMonth = month.coerceIn(months)
    val allowedDays = bounds.days(selectedYear, selectedMonth)
    val days = remember(allowedDays, dayRequired) { dayChoices(allowedDays, dayRequired) }
    val selectedDay = day?.coerceIn(allowedDays)
    val years = remember(minDate, maxDate) { (maxDate.year downTo minDate.year).map(Int::toString) }
    DatePickerDialog(onDismiss) {
        DatePickerHeading(dayRequired, onDismiss)
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(OtusColors.bgRegular, RoundedCornerShape(SheetTokens.radius))
                .padding(SheetTokens.gap),
            horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        ) {
            DatePickerColumn(
                "Day",
                days,
                selectedDay?.let { it - allowedDays.first + 1 } ?: 0,
                { day = if (it == 0) null else allowedDays.first + it - 1 },
                Modifier.weight(1f),
            )
            DatePickerColumn(
                "Month",
                MONTH_NAMES.slice((months.first - 1) until months.last),
                selectedMonth - months.first,
                {
                    month = months.first + it
                    day = day?.coerceIn(bounds.days(selectedYear, month))
                },
                Modifier.weight(CalendarTokens.MONTH_COLUMN_WEIGHT),
            )
            DatePickerColumn(
                "Year",
                years,
                maxDate.year - selectedYear,
                {
                    year = maxDate.year - it
                    month = selectedMonth.coerceIn(bounds.months(year))
                    day = day?.coerceIn(bounds.days(year, month))
                },
                Modifier.weight(CalendarTokens.YEAR_COLUMN_WEIGHT),
            )
        }
        DatePickerConfirm(dayRequired, selectedDay) {
            val chosen = YearMonth(selectedYear, selectedMonth)
            onConfirm(chosen, selectedDay?.let(chosen::onDay))
        }
    }
}

private fun dayChoices(
    days: IntRange,
    dayRequired: Boolean,
): List<String> = listOf(if (dayRequired) "Pick" else "Any") + days.map(Int::toString)

@Composable
private fun DatePickerConfirm(
    dayRequired: Boolean,
    day: Int?,
    onConfirm: () -> Unit,
) {
    ActionButton("Go", onConfirm, Modifier.fillMaxWidth(), primary = true, enabled = !dayRequired || day != null)
}

@Composable
private fun DatePickerColumn(
    title: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = OtusColors.text2, fontSize = SheetTokens.detailSize)
        WheelPicker(title, options, selected, onSelect, Modifier.weight(1f))
    }
}

private val MONTH_NAMES =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

@Composable
private fun DatePickerHeading(
    dayRequired: Boolean,
    onDismiss: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Choose date",
            Modifier.weight(1f).semantics { heading() },
            fontSize = SheetTokens.titleSize,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton("Close date chooser", onDismiss) { Symbol(Res.drawable.symbol_close) }
    }
    Text(
        if (dayRequired) "Choose a day to continue." else "Leave day as Any to choose a month.",
        color = OtusColors.text2,
        fontSize = SheetTokens.detailSize,
    )
}
