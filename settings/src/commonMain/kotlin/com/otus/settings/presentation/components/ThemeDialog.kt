package com.otus.settings.presentation.components

import com.otus.designsystem.theme.CustomTheme

/** The open theme popup: renaming or deleting a custom theme. */
internal sealed interface ThemeDialog {
    class Rename(
        val theme: CustomTheme,
    ) : ThemeDialog

    class Delete(
        val theme: CustomTheme,
    ) : ThemeDialog
}
