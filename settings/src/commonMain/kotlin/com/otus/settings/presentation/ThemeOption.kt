package com.otus.settings.presentation

import com.otus.designsystem.theme.CustomTheme
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice

/**
 * A theme on the theme page: its name, a description, and the [choice] that selects it. [custom]
 * is the custom theme it shows, null for a built-in one.
 */
internal class ThemeOption(
    val title: String,
    val description: String,
    val choice: ThemeChoice,
    val selected: Boolean,
    val custom: CustomTheme? = null,
)

/** The built-in themes, then the user's custom themes. */
internal val ThemeChoice.options: List<ThemeOption>
    get() =
        ThemeBase.entries.map { ThemeOption(it.title, it.description, withBase(it), customId == null && it == base) } +
            customThemes.map {
                ThemeOption(it.title, "Made from ${it.base.title}", withCustomTheme(it.id), it.id == customId, it)
            }

/** Why [name] cannot name [renaming], or a new theme while it is null; null if it can. */
internal fun ThemeChoice.nameProblem(
    name: String,
    renaming: CustomTheme?,
): String? =
    when {
        name.isBlank() -> "Enter a name"
        name.trim().length > MAX_NAME -> "Use $MAX_NAME characters or fewer"
        isTaken(name, renaming) -> "Another theme has this name"
        else -> null
    }

/** "Ink copy", or "Ink copy 2" and up when that name is taken. */
internal fun ThemeChoice.copyName(option: ThemeOption): String =
    generateSequence(1) { it + 1 }
        .map { if (it == 1) "${option.title} copy" else "${option.title} copy $it" }
        .first { !isTaken(it, null) }

/** Whether a theme other than [except] already has [name]. */
private fun ThemeChoice.isTaken(
    name: String,
    except: CustomTheme?,
): Boolean =
    options.any { option ->
        (option.custom == null || option.custom.id != except?.id) && option.title.equals(name.trim(), ignoreCase = true)
    }

private const val MAX_NAME = 30
