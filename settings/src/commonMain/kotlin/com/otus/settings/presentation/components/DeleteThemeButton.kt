package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otus.designsystem.components.ConfirmDialog
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.theme.CustomTheme
import com.otus.settings.presentation.title

/** Deletes [theme] with [onDelete] once the user confirms. */
@Composable
internal fun DeleteThemeButton(
    theme: CustomTheme,
    onDelete: () -> Unit,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    ActionButton("Delete ${theme.title}", { confirming = true })
    if (confirming) {
        ConfirmDialog(
            "Delete ${theme.title}?",
            "Its colors are deleted, and the app switches to ${theme.base.title}.",
            "Delete",
            onConfirm = onDelete,
            onDismiss = { confirming = false },
        )
    }
}
