package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.components.ActionDialog
import com.otus.designsystem.components.ConfirmDialog
import com.otus.designsystem.components.DialogAction
import com.otus.designsystem.components.InputDialog
import com.otus.designsystem.theme.ThemeChoice
import com.otus.settings.presentation.ThemeOption
import com.otus.settings.presentation.copyName
import com.otus.settings.presentation.nameProblem
import com.otus.settings.presentation.title

/**
 * Shows [dialog]. Every theme can be duplicated; a custom theme can also be renamed or deleted.
 * [onChange] applies the new choice, and [onDialog] opens the next popup or closes with null.
 */
@Composable
internal fun ThemeDialogs(
    dialog: ThemeDialog,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDialog: (ThemeDialog?) -> Unit,
) {
    when (dialog) {
        is ThemeDialog.Actions -> {
            ThemeActions(dialog.option, choice, onChange, onDialog)
        }

        is ThemeDialog.Rename -> {
            InputDialog(
                "Rename theme",
                "Name",
                "Rename",
                onConfirm = { onChange(choice.withName(dialog.theme.id, it)) },
                onDismiss = { onDialog(null) },
                initial = dialog.theme.title,
                problem = { choice.nameProblem(it, dialog.theme) },
            )
        }

        is ThemeDialog.Delete -> {
            ConfirmDialog(
                "Delete ${dialog.theme.title}?",
                "Its colors are deleted. If it is in use, the app switches to ${dialog.theme.base.title}.",
                "Delete",
                onConfirm = { onChange(choice.withoutCustomTheme(dialog.theme.id)) },
                onDismiss = { onDialog(null) },
            )
        }
    }
}

@Composable
private fun ThemeActions(
    option: ThemeOption,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDialog: (ThemeDialog?) -> Unit,
) {
    val custom = option.custom
    ActionDialog(
        option.title,
        buildList {
            custom?.let { add(DialogAction("Rename", { onDialog(ThemeDialog.Rename(it)) })) }
            add(DialogAction("Duplicate", { onChange(option.choice.withCopy(choice.copyName(option))) }))
            custom?.let { add(DialogAction("Delete", { onDialog(ThemeDialog.Delete(it)) })) }
        },
        onDismiss = { onDialog(null) },
    )
}
