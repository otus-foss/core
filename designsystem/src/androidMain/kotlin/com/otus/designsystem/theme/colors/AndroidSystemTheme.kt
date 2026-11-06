package com.otus.designsystem.theme.colors

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * The device's Material You colors from the wallpaper, in its current light or dark mode, laid out
 * like the built-in themes: in the dark, raised surfaces sit below the page; in the light, above
 * it. Null before Android 12, which has no Material You colors.
 */
public fun Context.systemTheme(): SystemTheme? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        null
    } else if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
        darkSystemTheme()
    } else {
        lightSystemTheme()
    }

@RequiresApi(Build.VERSION_CODES.S)
private fun Context.darkSystemTheme(): SystemTheme =
    SystemTheme(
        page = tone(android.R.color.system_neutral1_900),
        raised = lerp(tone(android.R.color.system_neutral1_900), tone(android.R.color.system_neutral1_1000), DEEPER),
        controls = tone(android.R.color.system_neutral2_800),
        borders = lerp(tone(android.R.color.system_neutral1_900), tone(android.R.color.system_neutral2_800), HALFWAY),
        text = tone(android.R.color.system_neutral1_50),
        secondaryText = tone(android.R.color.system_neutral2_200),
        mutedText = tone(android.R.color.system_neutral2_400),
        accent = tone(android.R.color.system_accent1_300),
    )

@RequiresApi(Build.VERSION_CODES.S)
private fun Context.lightSystemTheme(): SystemTheme =
    SystemTheme(
        page = tone(android.R.color.system_neutral1_50),
        raised = tone(android.R.color.system_neutral1_10),
        controls = lerp(tone(android.R.color.system_neutral2_100), tone(android.R.color.system_neutral2_200), HALFWAY),
        borders = tone(android.R.color.system_neutral2_100),
        text = tone(android.R.color.system_neutral1_900),
        secondaryText = tone(android.R.color.system_neutral2_700),
        mutedText = tone(android.R.color.system_neutral2_600),
        accent = tone(android.R.color.system_accent1_600),
    )

private fun Context.tone(
    @ColorRes id: Int,
): Color = Color(getColor(id))

/** How far the dark raised surface sits from the page towards black. */
private const val DEEPER = 0.35f
private const val HALFWAY = 0.5f
