package com.otus.designsystem.elements

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.AvatarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.panelTextStyle
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

/** The fill of a generated avatar with [colorIndex], from the theme's named colors. */
public fun avatarFill(colorIndex: Int): Color =
    when (colorIndex) {
        1 -> OtusColors.bgLight
        2 -> OtusColors.bgDark
        else -> OtusColors.accentDark
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
