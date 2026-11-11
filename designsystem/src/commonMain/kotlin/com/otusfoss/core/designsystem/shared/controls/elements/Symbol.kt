package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Decorative icon; the enclosing control supplies its accessible name. */
@Composable
public fun Symbol(
    resource: DrawableResource,
    mirrorInRtl: Boolean = false,
    size: Dp = Sizes.iconSize,
) {
    val mirrored = mirrorInRtl && LocalLayoutDirection.current == LayoutDirection.Rtl
    Icon(
        painterResource(resource),
        contentDescription = null,
        modifier = Modifier.size(size).scale(if (mirrored) -1f else 1f, 1f),
    )
}
