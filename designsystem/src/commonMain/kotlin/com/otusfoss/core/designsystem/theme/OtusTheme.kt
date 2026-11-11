package com.otusfoss.core.designsystem.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.OtusPalette

/** Applies [palette] to [OtusColors] and Material, then lays [content] on its page color. */
@Composable
public fun OtusTheme(
    palette: OtusPalette = OtusPalette.Otus,
    content: @Composable () -> Unit,
) {
    // Set before the content composes, so the first frame already uses the new colors.
    if (OtusColors.palette != palette) OtusColors.palette = palette
    val colors = remember(palette) { palette.materialColors() }
    MaterialTheme(colors = colors) {
        CompositionLocalProvider(
            LocalContentColor provides palette.text1,
            LocalContentAlpha provides 1f,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = palette.bgRegular,
                contentColor = palette.text1,
                content = content,
            )
        }
    }
}

private fun OtusPalette.materialColors(): Colors =
    Colors(
        primary = accentRegular,
        primaryVariant = accentDark,
        secondary = accentLight,
        secondaryVariant = accentDark,
        background = bgRegular,
        surface = bgRegular,
        error = accentLight,
        onPrimary = text1,
        onSecondary = text1,
        onBackground = text1,
        onSurface = text1,
        onError = text1,
        isLight = isLight,
    )
