package com.otus.settings.presentation

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.colors.ThemeChoice

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
