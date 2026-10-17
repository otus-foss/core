package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens

/**
 * Chooses a time of day on two wheels, hours and minutes, laid out like the
 * date chooser, in five-minute steps. [title] heads it; [confirmLabel] runs [onConfirm] with the hour and minute.
 */
@Composable
public fun TimeWheelPicker(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    var hour by rememberSaveable { mutableIntStateOf(initialHour.coerceIn(0, HOURS - 1)) }
    var minute by rememberSaveable { mutableIntStateOf(initialMinute / MINUTE_STEP) }
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
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(OtusColors.bgRegular, RoundedCornerShape(SheetTokens.radius))
                .padding(SheetTokens.gap),
            horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        ) {
            DatePickerColumn("Hour", (0 until HOURS).map(::twoDigits), hour, { hour = it }, Modifier.weight(1f))
            DatePickerColumn(
                "Minute",
                (0 until MINUTES step MINUTE_STEP).map(::twoDigits),
                minute,
                { minute = it },
                Modifier.weight(1f),
            )
        }
        ActionButton(confirmLabel, { onConfirm(hour, minute * MINUTE_STEP) }, Modifier.fillMaxWidth(), primary = true)
    }
}

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

private const val HOURS = 24
private const val MINUTES = 60
private const val MINUTE_STEP = 5
