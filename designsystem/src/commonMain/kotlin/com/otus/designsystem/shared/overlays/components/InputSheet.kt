package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.input.elements.InputField
import com.otus.designsystem.shared.overlays.elements.sheetInset
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A [SheetFrame] with one text field, such as a new name, focused when it opens. Its header button
 * reads [confirmLabel], passes the text to [onConfirm], and closes; [problem] explains why the text
 * cannot be used and disables that button; an untouched field shows no error yet.
 */
@Composable
public fun InputSheet(
    title: String,
    label: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initial: String = "",
    problem: (String) -> String? = { null },
) {
    var value by rememberSaveable { mutableStateOf(initial) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(focus) { focus.requestFocus() }
    val error = problem(value)
    SheetFrame(
        title,
        onDismiss,
        done =
            UiAction(confirmLabel, enabled = error == null, onClick = {
                onConfirm(value)
                onDismiss()
            }),
    ) {
        InputField(
            label,
            value,
            { value = it },
            Modifier
                .sheetInset()
                .padding(bottom = SheetTokens.inset)
                .focusRequester(focus),
            supportingText = error.takeIf { value != initial },
        )
    }
}
