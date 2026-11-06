package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.toPath
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/** The phone's metal frame and black bezel at the sides and rounded bottom, then its screen in [palette]. */
internal fun Modifier.phoneBottom(palette: OtusPalette): Modifier =
    phoneLayer(ThemeTokens.phoneFrame, ThemeTokens.phoneRadius, ThemeTokens.phoneFrameWidth)
        .phoneLayer(
            ThemeTokens.phoneBezel,
            ThemeTokens.phoneRadius - ThemeTokens.phoneFrameWidth,
            ThemeTokens.phoneBezelWidth,
        ).background(palette.bgRegular, bottomRounded(ThemeTokens.phoneScreenRadius))

/** The phone's frame and bezel at the sides only, above its rounded bottom, then its screen in [palette]. */
internal fun Modifier.phoneSides(palette: OtusPalette): Modifier =
    background(ThemeTokens.phoneFrame)
        .padding(horizontal = ThemeTokens.phoneFrameWidth)
        .background(ThemeTokens.phoneBezel)
        .padding(horizontal = ThemeTokens.phoneBezelWidth)
        .background(palette.bgRegular)

/** One ring of the phone: [color] in a shape rounded at the bottom by [radius], [width] thick at the sides and bottom. */
private fun Modifier.phoneLayer(
    color: Color,
    radius: Dp,
    width: Dp,
): Modifier =
    background(color, bottomRounded(radius))
        .padding(start = width, end = width, bottom = width)

internal fun bottomRounded(radius: Dp): Shape =
    SmoothCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(radius), CornerSize(radius))

/**
 * A hairline in [color] around [shape]'s bottom, and its sides too with [sides], over the content.
 * The top always runs past the edge, since it sits under the control panel.
 */
internal fun Modifier.edge(
    shape: Shape,
    color: Color,
    sides: Boolean,
): Modifier =
    drawWithCache {
        val width = PanelTokens.borderWidth.toPx()
        val inset = if (sides) width / 2 else -width
        val outline =
            shape.toPath(
                Rect(inset, -width, size.width - inset, size.height - width / 2),
                layoutDirection,
                this,
            )
        onDrawWithContent {
            drawContent()
            clipRect { drawPath(outline, color, style = Stroke(width)) }
        }
    }

/**
 * Gives [content] [palette]'s text color. A page's text style carries the app theme's text color,
 * which would win over the content color, so the style is replaced too.
 */
@Composable
internal fun PhoneColors(
    palette: OtusPalette,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides palette.text1,
        LocalTextStyle provides LocalTextStyle.current.copy(color = palette.text1),
        content = content,
    )
}
