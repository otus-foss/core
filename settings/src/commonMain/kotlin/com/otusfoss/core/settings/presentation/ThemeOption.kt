package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.colors.CustomTheme
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice

/**
 * A theme on the theme page: its name and the [choice] that selects it. [custom] is the custom
 * theme it shows, null for a built-in one; [system] marks the device's own colors.
 */
internal class ThemeOption(
    val title: String,
    val choice: ThemeChoice,
    val selected: Boolean,
    val custom: CustomTheme? = null,
    val system: Boolean = false,
)

/** The built-in themes, the device's colors where it has them, then the user's custom themes. */
internal val ThemeChoice.options: List<ThemeOption>
    get() =
        ThemeBase.entries.map { builtInOption(it) } + listOfNotNull(systemOption) +
            customThemes.map { customOption(it) }

private fun ThemeChoice.builtInOption(theme: ThemeBase): ThemeOption =
    ThemeOption(theme.title, withBase(theme), customId == null && activeSystem == null && theme == base)

private val ThemeChoice.systemOption: ThemeOption?
    get() = system?.let { ThemeOption("System", withSystem(), activeSystem != null, system = true) }

private fun ThemeChoice.customOption(theme: CustomTheme): ThemeOption =
    ThemeOption(theme.title, withCustomTheme(theme.id), theme.id == customId, theme)

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
        .first { !isTaken(it, except = null) }

/** Whether a theme other than [except] already has [name]. */
private fun ThemeChoice.isTaken(
    name: String,
    except: CustomTheme?,
): Boolean =
    options.any { option ->
        (option.custom == null || option.custom.id != except?.id) && option.title.equals(name.trim(), ignoreCase = true)
    }

private const val MAX_NAME = 30
