package com.otus.designsystem.shared.settings.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * [colors] as overlapping circles, like an [com.otus.designsystem.shared.people.components.AvatarStack], each
 * outlined so a color that matches the page still shows. It only shows the colors; the enclosing
 * row acts and supplies the accessible name.
 */
@Composable
public fun SwatchStack(
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-ThemeTokens.stackOverlap)) {
        colors.forEach {
            Box(
                Modifier
                    .size(ThemeTokens.stackSwatch)
                    .background(it, CircleShape)
                    .border(Sizes.borderWidth, OtusColors.bgLight, CircleShape),
            )
        }
    }
}
