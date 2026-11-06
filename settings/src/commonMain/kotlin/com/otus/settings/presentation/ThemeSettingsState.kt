package com.otus.settings.presentation

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.storage.StoredChoice

/**
 * The user's theme and accent. Pass [palette] to `OtusTheme` so every screen follows it; the
 * theme page calls [select].
 */
public class ThemeSettingsState(
    store: StoredChoice,
) : StoredChoiceState<ThemeChoice>(store, ThemeChoice(), ::decodeThemeChoice, { it.encode() }) {
    public val choice: ThemeChoice get() = value

    /** Every color of [choice], worked out again only when it changes. */
    public val palette: OtusPalette by derivedStateOf { value.palette }
}
