package com.otus.designsystem.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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

@Composable
public fun Avatar(
    initial: String,
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,
    colorIndex: Int = 0,
    size: Dp = AvatarTokens.size,
    ring: BorderStroke? = null,
    fill: Color = avatarFill(colorIndex),
) {
    Box(
        modifier.size(size).clip(CircleShape).background(fill),
        contentAlignment = Alignment.Center,
    ) {
        if (image == null) {
            Text(initial, style = initialsStyle(size), color = OtusColors.text1)
        } else {
            Image(
                image,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (ring != null) Box(Modifier.matchParentSize().border(ring, CircleShape))
    }
}

internal fun avatarFill(colorIndex: Int): Color =
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
