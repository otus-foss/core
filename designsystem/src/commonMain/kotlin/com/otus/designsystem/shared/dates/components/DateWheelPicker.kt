package com.otus.designsystem.shared.dates.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.otus.dateformat.fullName
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.selection.elements.WheelPicker
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.selection.CalendarTokens
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
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
        DatePickerHeading("Choose date", if (dayRequired) DAY_REQUIRED else DAY_OPTIONAL, onDismiss)
        DateWheels(Modifier.weight(1f)) {
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
        ActionButton(
            "Go",
            {
                val chosen = YearMonth(selectedYear, selectedMonth)
                onConfirm(chosen, selectedDay?.let(chosen::onDay))
            },
            Modifier.fillMaxWidth(),
            primary = true,
            enabled = !dayRequired || selectedDay != null,
        )
    }
}

private fun dayChoices(
    days: IntRange,
    dayRequired: Boolean,
): List<String> = listOf(if (dayRequired) "Pick" else "Any") + days.map(Int::toString)

/** The wheels of a date picker side by side on a `bgRegular` card. */
@Composable
internal fun DateWheels(
    modifier: Modifier,
    wheels: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(OtusColors.bgRegular, SmoothCornerShape(SheetTokens.radius))
            .padding(SheetTokens.gap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        content = wheels,
    )
}

@Composable
internal fun DatePickerColumn(
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

private const val DAY_REQUIRED = "Choose a day to continue."
private const val DAY_OPTIONAL = "Leave day as Any to choose a month."

internal val MONTH_NAMES: List<String> = Month.entries.map { it.fullName }

/** A date picker's [title], its close button, and a `text2` [note] on how to choose. */
@Composable
internal fun DatePickerHeading(
    title: String,
    note: String?,
    onDismiss: () -> Unit,
) {
    PickerTitle(title, "Close date chooser", onDismiss)
    if (note != null) Text(note, color = OtusColors.text2, fontSize = SheetTokens.detailSize)
}

/** A wheel picker's [title] and its close button, which screen readers hear as [closeLabel]. */
@Composable
internal fun PickerTitle(
    title: String,
    closeLabel: String,
    onDismiss: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            Modifier.weight(1f).semantics { heading() },
            fontSize = SheetTokens.titleSize,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(closeLabel, onDismiss) { Symbol(Res.drawable.symbol_close) }
    }
}
