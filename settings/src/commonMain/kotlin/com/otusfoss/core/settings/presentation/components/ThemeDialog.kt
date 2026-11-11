package com.otusfoss.core.settings.presentation.components

import com.otusfoss.core.designsystem.theme.colors.CustomTheme

/** The open theme popup: renaming or deleting a custom theme. */
internal sealed interface ThemeDialog {
    class Rename(
        val theme: CustomTheme,
    ) : ThemeDialog

    class Delete(
        val theme: CustomTheme,
    ) : ThemeDialog
}
