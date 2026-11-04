package com.otus.designsystem.shared.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/** Whether the setting rows here take taps; [DependentSettings] turns it off. */
public val LocalSettingsEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

/**
 * Setting rows that only apply while another setting is set, such as a sender's order while the
 * sender shows. Unless [enabled], every [SettingRow], [SettingToggleRow], and [SettingPageRow] in
 * [content] stays where it is, draws its title, icon, value, and switch dimmed, and ignores taps;
 * give each a description that says what it waits on. Keeping the rows rather than hiding them
 * keeps the page's shape, and search still finds them.
 */
@Composable
public fun DependentSettings(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val shown = enabled && LocalSettingsEnabled.current
    CompositionLocalProvider(LocalSettingsEnabled provides shown, content = content)
}
