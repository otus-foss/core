package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

/** Decorative icon; the enclosing control supplies its accessible name. */
@Composable
public fun Symbol(
    resource: DrawableResource,
    mirrorInRtl: Boolean = false,
    size: Dp = Sizes.iconSize,
    tone: SymbolTone = SymbolTone.Duo,
) {
    val mirrored = mirrorInRtl && LocalLayoutDirection.current == LayoutDirection.Rtl
    val modifier = Modifier.size(size).scale(if (mirrored) -1f else 1f, 1f)
    when (tone) {
        SymbolTone.Duo -> Icon(painterResource(resource), contentDescription = null, modifier)
        else -> Icon(paintedPainter(resource, tone), null, modifier, tint = Color.Unspecified)
    }
}

@Composable
private fun paintedPainter(
    resource: DrawableResource,
    tone: SymbolTone,
): Painter {
    val vector = vectorResource(resource)
    val front = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val back = if (tone == SymbolTone.Solid) front else lerp(OtusColors.bgRegular, front, SHADE)
    return rememberVectorPainter(remember(vector, front, back) { vector.painted(front, back) })
}

/** How much of the content color the back shade keeps; a little more than the drawables' 0.45. */
private const val SHADE = 0.55f
