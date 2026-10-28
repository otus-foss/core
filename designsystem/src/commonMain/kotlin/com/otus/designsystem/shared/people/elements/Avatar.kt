package com.otus.designsystem.shared.people.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.people.AvatarTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A circle with [image], or else [symbol], or else [initial] on [fill]. An empty [initial] leaves the
 * color alone. [content] colors the symbol and the initial. For a ring, such as one that separates
 * overlapping avatars, pass `Modifier.border(stroke, CircleShape)`.
 */
@Composable
public fun Avatar(
    initial: String,
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,
    size: Dp = AvatarTokens.size,
    fill: Color = avatarFill(0),
    symbol: DrawableResource? = null,
    content: Color = OtusColors.text1,
) {
    Box(
        modifier.size(size).clip(CircleShape).background(fill),
        contentAlignment = Alignment.Center,
    ) {
        if (image == null && symbol != null) {
            CompositionLocalProvider(LocalContentColor provides content) {
                Symbol(symbol, size = size * AvatarTokens.SYMBOL_SCALE)
            }
        } else if (image == null) {
            Text(initial, style = initialsStyle(size), color = content)
        } else {
            Image(
                image,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/**
 * The fill of a generated avatar with [colorIndex]: the theme's `bgLight`, a grey a step toward
 * `bgDark`, or one a step toward `text3`. A list of people stays neutral instead of taking the
 * accent, and every fill shows on the raised cards rows sit on.
 */
public fun avatarFill(colorIndex: Int): Color =
    when (colorIndex) {
        1 -> OtusColors.bgLight
        2 -> lerp(OtusColors.bgLight, OtusColors.text3, AvatarTokens.FILL_STEP)
        else -> lerp(OtusColors.bgLight, OtusColors.bgDark, AvatarTokens.FILL_STEP)
    }

/** Smaller avatars scale their initials with the circle so they stay inside the shape. */
@Composable
private fun initialsStyle(size: Dp): TextStyle {
    val style = panelTextStyle()
    if (size == AvatarTokens.size) return style
    val fontSize = with(LocalDensity.current) { (size * INITIALS_SCALE).toSp() }
    return style.copy(fontSize = fontSize, lineHeight = fontSize)
}

private const val INITIALS_SCALE = 1f / 3
