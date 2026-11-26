package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.typography.AppFont
import com.otusfoss.core.storage.StoredChoice

/**
 * The user's app font. Pass its [font] to `OtusTheme`; the appearance page calls [select]. An
 * unknown saved name reads as the system font.
 */
public class FontSettingsState(
    store: StoredChoice,
) : StoredChoiceState<AppFont>(
        store,
        AppFont.System,
        { entryNamed<AppFont>(it.trim()) },
        { it.name.lowercase() },
    ) {
    public val font: AppFont get() = value
}
