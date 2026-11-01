package com.otus.settings.presentation

import com.otus.dateformat.DateFormatPreference
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_calendar_month
import com.otus.designsystem.resources.symbol_owl
import com.otus.designsystem.resources.symbol_palette
import com.otus.designsystem.resources.symbol_speed
import com.otus.designsystem.theme.brand.AppIconStyle
import com.otus.designsystem.theme.motion.AnimationSpeed
import com.otus.settings.presentation.components.label
import com.otus.settings.presentation.screens.AppearanceSettingsScreen
import com.otus.settings.presentation.screens.label
import com.otus.settings.presentation.screens.shortLabel

/**
 * What search finds on [AppearanceSettingsScreen]: the Theme page, which [openTheme] shows, and the
 * Look settings. [look] adds the app's own rows, as the screen's `look` slot does.
 */
public fun SettingsIndexScope.appearanceIndex(
    dateFormat: DateFormatSettingsState,
    theme: ThemeSettingsState,
    animation: AnimationSettingsState,
    openTheme: (setting: String?) -> Unit,
    look: SettingsIndexScope.() -> Unit = {},
) {
    section("Look") {
        page(
            "Theme",
            openTheme,
            Res.drawable.symbol_palette,
            value = theme.choice.themeTitle,
            options = theme.choice.options.map { it.title },
        )
        look()
        setting(
            "Date format",
            dateFormat.preference.shortLabel,
            DateFormatPreference.entries.map { it.patternLabel },
            Res.drawable.symbol_calendar_month,
        )
        setting("Animations", animation.speed.label, AnimationSpeed.entries.map { it.label }, Res.drawable.symbol_speed)
    }
}

/** The App icon row of [com.otus.settings.presentation.components.AppIconSetting], for search. */
public fun SettingsIndexScope.appIconIndex(appIcon: AppIconSettingsState) {
    setting("App icon", appIcon.style.label, AppIconStyle.entries.map { it.label }, Res.drawable.symbol_owl)
}
