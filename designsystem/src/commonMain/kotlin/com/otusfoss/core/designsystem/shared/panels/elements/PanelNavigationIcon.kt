package com.otusfoss.core.designsystem.shared.panels.elements

import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_left
import com.otusfoss.core.designsystem.resources.symbol_close
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * What the header's leading button shows: the ‹ chevron, or ✕ to leave a mode such as selecting.
 * Each glyph draws at [size], and its ink starts [inkStart] of the way across its symbol, so a new
 * glyph or a redrawn symbol must update that fraction to keep the button lined up.
 */
public enum class PanelNavigationIcon(
    internal val symbol: DrawableResource,
    internal val size: Dp,
    private val inkStart: Float,
) {
    Back(Res.drawable.symbol_chevron_left, PanelTokens.navigationIconSize, inkStart = 337f / 960f),
    Close(Res.drawable.symbol_close, PanelTokens.closeIconSize, inkStart = 217f / 960f),
    ;

    /** How far the glyph sits from the target's start, so its ink starts where the chevron's does. */
    internal val offset: Dp
        get() = Back.ink - ink

    private val ink: Dp
        get() = size * inkStart
}
