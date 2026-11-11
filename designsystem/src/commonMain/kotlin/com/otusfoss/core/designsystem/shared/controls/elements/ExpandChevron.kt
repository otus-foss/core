package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes

/** A chevron that points along the line while collapsed and turns down once [expanded]. */
@Composable
internal fun ExpandChevron(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.iconSize,
) {
    // A chevron mirrored for right-to-left turns the other way to point down.
    val turn = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val degrees by animateFloatAsState(
        if (expanded) ControlTokens.CHEVRON_OPEN_DEGREES * turn else 0f,
        motionTween(),
        label = "Expand chevron",
    )
    Box(modifier.rotate(degrees)) { Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true, size = size) }
}
