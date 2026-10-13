package com.otus.settings.presentation

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.parseHexColor
import com.otus.designsystem.theme.toHex

/** Keeps the encoded [ThemeChoice] between launches, such as in platform preferences. */
public interface ThemePreferenceStore {
    public fun read(): String?

    public fun write(value: String)
}

/**
 * The user's theme and accent. Pass [palette] to `OtusTheme` so every screen follows it; the
 * theme page calls [select].
 */
public class ThemeSettingsState(
    private val store: ThemePreferenceStore? = null,
    initialChoice: ThemeChoice = store?.read()?.let(::decodeThemeChoice) ?: ThemeChoice(),
) {
    public var choice: ThemeChoice by mutableStateOf(initialChoice)
        private set

    /** Every color of [choice], worked out again only when it changes. */
    public val palette: OtusPalette by derivedStateOf { choice.palette }

    public fun select(choice: ThemeChoice) {
        store?.write(choice.encode())
        this.choice = choice
    }
}

/** Stores a choice as its theme and accent, such as `light #2383E2`. */
internal fun ThemeChoice.encode(): String = "${base.name.lowercase()} ${accent.toHex()}"

/** Reads [encode]'s format; an unknown theme or color falls back to the default for that part. */
internal fun decodeThemeChoice(value: String): ThemeChoice {
    val parts = value.trim().split(' ')
    val default = ThemeChoice()
    return ThemeChoice(
        base = ThemeBase.entries.firstOrNull { it.name.equals(parts.first(), ignoreCase = true) } ?: default.base,
        accent = parts.getOrNull(1)?.let(::parseHexColor) ?: default.accent,
    )
}
