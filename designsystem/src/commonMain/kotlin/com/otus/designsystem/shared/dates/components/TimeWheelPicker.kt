package com.otus.designsystem.shared.dates.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.elements.ActionButton
import kotlinx.datetime.LocalTime
import kotlin.math.abs

/**
 * Chooses a time of day on two wheels, hours and minutes in five-minute steps, laid out like the
 * date chooser. Only times within [allowed] show, such as none before now for a message sent
 * today; a range without a five-minute step, such as 10:01 to 10:04, offers its start alone.
 * [title] heads it; [confirmLabel] runs [onConfirm] with the chosen time.
 */
@Composable
public fun TimeWheelPicker(
    title: String,
    initial: LocalTime,
    allowed: ClosedRange<LocalTime>,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val times = remember(allowed) { pickableTimes(allowed) }
    var chosen by rememberSaveable { mutableIntStateOf(times.closestTo(initial)) }
    val hours = times.map { it.hour }.distinct()
    val time = times[chosen.coerceIn(times.indices)]
    val minutes = times.filter { it.hour == time.hour }
    DatePickerDialog(onDismiss) {
        PickerTitle(title, "Close time chooser", onDismiss)
        DateWheels(Modifier.weight(1f)) {
            DatePickerColumn(
                "Hour",
                hours.map(::twoDigits),
                hours.indexOf(time.hour),
                // A new hour keeps the minute where that hour allows it, else its nearest one.
                { chosen = times.closestTo(LocalTime(hours[it], time.minute)) },
                Modifier.weight(1f),
            )
            DatePickerColumn(
                "Minute",
                minutes.map { twoDigits(it.minute) },
                minutes.indexOf(time),
                { chosen = times.indexOf(minutes[it]) },
                Modifier.weight(1f),
            )
        }
        ActionButton(confirmLabel, { onConfirm(time) }, Modifier.fillMaxWidth(), primary = true)
    }
}

/** Every five-minute step within [allowed], from its first step on or after its start. */
internal fun stepTimes(allowed: ClosedRange<LocalTime>): List<LocalTime> {
    val first = (allowed.start.hour * MINUTES + allowed.start.minute + MINUTE_STEP - 1) / MINUTE_STEP * MINUTE_STEP
    val last = allowed.endInclusive.hour * MINUTES + allowed.endInclusive.minute
    return (first..last step MINUTE_STEP).map { LocalTime(it / MINUTES, it % MINUTES) }
}

/** The times [TimeWheelPicker] offers: [stepTimes], or the start of [allowed] when it has none. */
internal fun pickableTimes(allowed: ClosedRange<LocalTime>): List<LocalTime> =
    stepTimes(allowed).ifEmpty { listOf(allowed.start) }

/** The index of the time nearest [time], or the first. */
private fun List<LocalTime>.closestTo(time: LocalTime): Int =
    indices.minByOrNull { abs(this[it].toSecondOfDay() - time.toSecondOfDay()) } ?: 0

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

private const val MINUTES = 60
private const val MINUTE_STEP = 5
