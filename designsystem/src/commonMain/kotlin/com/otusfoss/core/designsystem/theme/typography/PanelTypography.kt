package com.otusfoss.core.designsystem.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import com.otusfoss.core.designsystem.theme.colors.OtusColors

/** Body text in the theme's [AppFont]. */
@Composable
public fun panelTextStyle(): TextStyle {
    val family = fontFamily(LocalAppFont.current)
    val color = OtusColors.text1
    return remember(family, color) {
        TextStyle(
            fontFamily = family,
            fontSize = TypeScale.body,
            lineHeight = TypeScale.bodyLineHeight,
            color = color,
        )
    }
}
