package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.overlays.components.ConfirmDialog
import com.otus.designsystem.shared.overlays.components.InputDialog
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.theme.colors.CustomTheme
import com.otus.designsystem.theme.colors.ThemeChoice
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
        if (custom != null) add(MenuAction("Rename") { onDialog(ThemeDialog.Rename(custom)) })
        add(MenuAction("Duplicate") { onChange(option.choice.withCopy(choice.copyName(option))) })
        if (custom != null) add(MenuAction("Delete") { onDialog(ThemeDialog.Delete(custom)) })
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
        is ThemeDialog.Rename -> RenameThemeDialog(dialog.theme, choice, onChange) { onDialog(null) }
        is ThemeDialog.Delete -> DeleteThemeDialog(dialog.theme, choice, onChange) { onDialog(null) }
    }
}

@Composable
private fun RenameThemeDialog(
    theme: CustomTheme,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    InputDialog(
        title = "Rename theme",
        label = "Name",
        confirmLabel = "Rename",
        onConfirm = { onChange(choice.withName(theme.id, it)) },
        onDismiss = onDismiss,
        initial = theme.title,
        problem = { choice.nameProblem(it, theme) },
    )
}

@Composable
private fun DeleteThemeDialog(
    theme: CustomTheme,
    choice: ThemeChoice,
    onChange: (ThemeChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = "Delete ${theme.title}?",
        message = "Its colors are deleted. If it is in use, the app switches to ${theme.base.title}.",
        confirmLabel = "Delete",
        onConfirm = { onChange(choice.withoutCustomTheme(theme.id)) },
        onDismiss = onDismiss,
    )
}
