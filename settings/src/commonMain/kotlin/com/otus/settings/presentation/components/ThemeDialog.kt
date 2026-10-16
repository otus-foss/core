package com.otus.settings.presentation.components

import com.otus.designsystem.theme.CustomTheme
import com.otus.settings.presentation.ThemeOption

/** The open theme popup: a theme's actions, or renaming or deleting a custom theme. */
internal sealed interface ThemeDialog {
    class Actions(
        val option: ThemeOption,
    ) : ThemeDialog

    class Rename(
        val theme: CustomTheme,
    ) : ThemeDialog

    class Delete(
        val theme: CustomTheme,
    ) : ThemeDialog
}
