package com.otus.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.SheetTokens
import kotlinx.datetime.LocalTime
import kotlin.math.abs

/**
 * Chooses a time of day on two wheels, hours and minutes in five-minute steps, laid out like the
 * date chooser. Only times within [allowed] show, such as none before now for a message sent
 * today. [title] heads it; [confirmLabel] runs [onConfirm] with the chosen time.
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
    val times = remember(allowed) { stepTimes(allowed) }
    var chosen by rememberSaveable { mutableIntStateOf(times.closestTo(initial)) }
    val hours = times.map { it.hour }.distinct()
    val time = times[chosen.coerceIn(times.indices)]
    val minutes = times.filter { it.hour == time.hour }
    DatePickerDialog(onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                Modifier.weight(1f).semantics {
                    heading()
                },
                fontSize = SheetTokens.titleSize,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton("Close time chooser", onDismiss) { Symbol(Res.drawable.symbol_close) }
        }
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

/** The index of the time nearest [time], or the first. */
private fun List<LocalTime>.closestTo(time: LocalTime): Int =
    indices.minByOrNull { abs(this[it].toSecondOfDay() - time.toSecondOfDay()) } ?: 0

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

private const val MINUTES = 60
private const val MINUTE_STEP = 5
