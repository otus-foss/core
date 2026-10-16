package com.otus.settings.presentation

import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice

/** A theme on the theme page: its name, a description, and the [choice] that selects it. */
internal class ThemeOption(
    val title: String,
    val description: String,
    val choice: ThemeChoice,
    val selected: Boolean,
)

/** The built-in themes, then the user's custom themes. */
internal val ThemeChoice.options: List<ThemeOption>
    get() =
        ThemeBase.entries.map { ThemeOption(it.title, it.description, withBase(it), customId == null && it == base) } +
            customThemes.map {
                ThemeOption(it.title, "Made from ${it.base.title}", withCustomTheme(it.id), it.id == customId)
            }
