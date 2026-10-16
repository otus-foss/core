package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.components.ConfirmDialog
import com.otus.designsystem.components.InputDialog
import com.otus.designsystem.components.MenuAction
import com.otus.designsystem.theme.ThemeChoice
import com.otus.settings.presentation.ThemeOption
import com.otus.settings.presentation.copyName
import com.otus.settings.presentation.nameProblem
import com.otus.settings.presentation.title

/**
 * The menu for one theme card: every theme can be duplicated, and a custom theme can also be
 * renamed or deleted. [onChange] applies a duplicate, and [onDialog] opens the rename or delete
 * popup.
 */
internal fun themeActions(
    option: ThemeOption,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDialog: (ThemeDialog) -> Unit,
): List<MenuAction> {
    val custom = option.custom
    return buildList {
        custom?.let { add(MenuAction("Rename") { onDialog(ThemeDialog.Rename(it)) }) }
        add(MenuAction("Duplicate") { onChange(option.choice.withCopy(choice.copyName(option))) })
        custom?.let { add(MenuAction("Delete") { onDialog(ThemeDialog.Delete(it)) }) }
    }
}

/** Shows [dialog]; [onChange] applies the new choice, and [onDialog] closes it with null. */
@Composable
internal fun ThemeDialogs(
    dialog: ThemeDialog,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDialog: (ThemeDialog?) -> Unit,
) {
    when (dialog) {
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
