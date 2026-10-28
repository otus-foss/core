package com.otus.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.brand.AppIconStyle

/** Keeps the chosen [AppIconStyle]'s name between launches, such as in platform preferences. */
public interface AppIconPreferenceStore {
    public fun read(): String?

    public fun write(value: String)
}

/**
 * How the app icon and launch animation color the owl. The app switches its launcher icon to
 * follow [style]; the appearance page calls [select].
 */
public class AppIconSettingsState(
    private val store: AppIconPreferenceStore? = null,
    initialStyle: AppIconStyle = store?.read()?.let(::decodeAppIconStyle) ?: AppIconStyle.Accent,
) {
    public var style: AppIconStyle by mutableStateOf(initialStyle)
        private set

    public fun select(style: AppIconStyle) {
        store?.write(style.name.lowercase())
        this.style = style
    }
}

/** An unknown name reads as [AppIconStyle.Accent]. */
internal fun decodeAppIconStyle(value: String): AppIconStyle =
    AppIconStyle.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: AppIconStyle.Accent
