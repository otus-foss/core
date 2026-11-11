package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.brand.AppIconStyle
import com.otusfoss.core.storage.StoredChoice

/**
 * How the app icon and launch animation color the owl. The app switches its launcher icon to
 * follow [style]; the appearance page calls [select]. An unknown saved name reads as Accent.
 */
public class AppIconSettingsState(
    store: StoredChoice,
) : StoredChoiceState<AppIconStyle>(
        store,
        AppIconStyle.Accent,
        { entryNamed<AppIconStyle>(it.trim()) },
        { it.name.lowercase() },
    ) {
    public val style: AppIconStyle get() = value
}
