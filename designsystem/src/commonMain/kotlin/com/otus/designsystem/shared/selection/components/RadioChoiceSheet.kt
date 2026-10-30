package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.overlays.components.SheetFrame

/**
 * A sheet of [choices] as radio rows, each reading as its [row]'s label over its detail, starting
 * at the selected one. Unlike [ChoiceSheet], picking only moves the radio: Done saves the picked
 * option through [onSave] while [doneEnabled] allows it, and the scrim, Back, or a swipe down
 * discards it. [below] adds content under the rows for the picked option, such as a field it needs.
 */
@Composable
public fun <T> RadioChoiceSheet(
    title: String,
    choices: ChoiceOptions<T>,
    row: (T) -> ChoiceRow,
    onSave: (T) -> Unit,
    onDismiss: () -> Unit,
    doneEnabled: (T) -> Boolean = { true },
    below: @Composable ColumnScope.(T) -> Unit = {},
) {
    // The index survives process death for any option type; the option itself might not be saveable.
    var pickedIndex by rememberSaveable { mutableIntStateOf(choices.options.indexOf(choices.selected)) }
    val picked = choices.options.getOrElse(pickedIndex) { choices.selected }
    SheetFrame(
        title,
        onDismiss,
        onDone = {
            onSave(picked)
            onDismiss()
        },
        doneEnabled = doneEnabled(picked),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            choices.options.forEachIndexed { index, option ->
                val shown = row(option)
                PickRow(
                    shown.label,
                    shown.detail.orEmpty(),
                    PickMark(index == pickedIndex, atStart = true),
                    { pickedIndex = index },
                    layout = PickRowLayout(subtitleLines = DETAIL_LINES),
                )
            }
            below(picked)
        }
    }
}

/** Enough lines for a sentence of detail on a narrow phone. */
private const val DETAIL_LINES = 2
