package com.otus.designsystem.shared.dates.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.otus.dateformat.DateFormatter
import com.otus.dateformat.currentLocalDate
import com.otus.dateformat.formatRelative
import com.otus.dateformat.formatRelativeRange
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.animateScrollToItem
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.selection.CalendarTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth

/**
 * Picks a range of dates up to today: a summary of the range, shortcuts such as Last 7 days, and a
 * [DateRangeCalendar] in the rest of its room. [start] and [end] are ISO dates; [onChange] gets the
 * new pair, with a null end while only the start is chosen.
 */
@Composable
public fun DateRangePicker(
    start: String?,
    end: String?,
    dateFormatter: DateFormatter,
    onChange: (String?, String?) -> Unit,
) {
    val calendar = rememberRangeCalendar(start)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val controlsHeight = maxHeight * CalendarTokens.CONTROLS_HEIGHT_FRACTION
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(SheetTokens.gap)) {
            Column(
                Modifier.heightIn(max = controlsHeight).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
            ) {
                DateRangeSummary(start, end, dateFormatter, calendar.today)
                DateRangeShortcuts(calendar.today, start, end) { first, last ->
                    calendar.goToMonth(calendar.today)
                    onChange(first, last)
                }
            }
            RangeCalendarBody(calendar, start, end, onChange, Modifier.weight(1f))
        }
    }
}

/**
 * The calendar of a [DateRangePicker] on its own, for a page that offers its own shortcuts, such
 * as the Date page of a filter panel. It fills the height [modifier] gives it, which must be
 * bounded, such as [com.otus.designsystem.theme.tokens.selection.CalendarTokens.rangeCalendarHeight] inside a
 * scrolling page. Taps pick the start and then the end, and a hold and drag picks a range.
 */
@Composable
public fun DateRangeCalendar(
    start: String?,
    end: String?,
    onChange: (String?, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    RangeCalendarBody(rememberRangeCalendar(start), start, end, onChange, modifier)
}

/**
 * Today, the weeks a range calendar shows, and its scroll position, which opens on [start]'s month
 * and moves to another on [scroll].
 */
private class RangeCalendar(
    val today: LocalDate,
    val weeks: CalendarWeeks,
    val listState: LazyListState,
    private val scope: CoroutineScope,
    private val scroll: AnimationSpec<Float>,
) {
    fun goToMonth(month: LocalDate) {
        scope.launch { listState.animateScrollToItem(weeks.indexOf(month.yearMonth.firstDay), scroll) }
    }
}

@Composable
private fun rememberRangeCalendar(start: String?): RangeCalendar {
    val today = currentLocalDate()
    val initialMonth = LocalDate.parse((start ?: today.toString()).take(YEAR_MONTH_LENGTH) + "-01")
    val weeks = remember(today) { CalendarWeeks(today) }
    val listState = rememberLazyListState(weeks.indexOf(initialMonth))
    val scope = rememberCoroutineScope()
    val scroll = motionTween<Float>(MotionSpeed.Relaxed)
    return remember(today, weeks, listState, scope, scroll) { RangeCalendar(today, weeks, listState, scope, scroll) }
}

@Composable
private fun RangeCalendarBody(
    calendar: RangeCalendar,
    start: String?,
    end: String?,
    onChange: (String?, String?) -> Unit,
    modifier: Modifier,
) {
    val today = calendar.today
    Box(modifier) {
        ContinuousCalendar(
            state = calendar.listState,
            start = start,
            end = end,
            weeks = calendar.weeks,
            onMonth = calendar::goToMonth,
            onRange = onChange,
            onDate = { chosen, throughToday ->
                val date = chosen.toString()
                when {
                    chosen > today -> Unit
                    throughToday -> onChange(minOf(chosen, today).toString(), maxOf(chosen, today).toString())
                    start == null || end != null -> onChange(date, null)
                    date < start -> onChange(date, start)
                    else -> onChange(start, date)
                }
            },
        )
    }
}

private const val YEAR_MONTH_LENGTH = 7
private const val WEEK_LENGTH = 7
private const val THIRTY_DAYS = 30

@Composable
private fun DateRangeSummary(
    start: String?,
    end: String?,
    dateFormatter: DateFormatter,
    today: LocalDate,
) {
    Text(
        when {
            start == null -> "Tap dates or hold and drag to select."
            end == null -> "${dateFormatter.formatRelative(LocalDate.parse(start), today)} → Choose an end date"
            else -> dateFormatter.formatRelativeRange(LocalDate.parse(start), LocalDate.parse(end), today)
        },
        color = OtusColors.text2,
        maxLines = 2,
    )
}

@Composable
private fun DateRangeShortcuts(
    today: LocalDate,
    start: String?,
    end: String?,
    onChoose: (String, String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
    ) {
        listOf("Today" to 1, "Last 7 days" to WEEK_LENGTH, "Last 30 days" to THIRTY_DAYS).forEach { (label, days) ->
            val first = today.minus(days - 1, DateTimeUnit.DAY).toString()
            val last = today.toString()
            val active = start == first && end == last
            ActionButton(
                label,
                { onChoose(first, last) },
                modifier = Modifier.semantics { selected = active },
                primary = active,
            )
        }
    }
}
