package com.otusfoss.core.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.settings.ThemeTokens

/**
 * [palette]'s colors as overlapping rings in two rows: its surfaces above, its text and accent
 * below, half a ring along. Each ring is outlined in the card's color, so overlaps read as cut out.
 */
@Composable
internal fun ThemeRings(palette: OtusPalette) {
    val rows = listOf(palette.surfaces, palette.inks)
    Box(Modifier.fillMaxWidth().height(ThemeTokens.sketchHeight), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(
                width = ThemeTokens.ringSize + ThemeTokens.ringStep * (rows.maxOf { it.size } - 1),
                height = ThemeTokens.ringSize + ThemeTokens.ringDrop * (rows.size - 1),
            ),
        ) {
            rows.forEachIndexed { row, colors ->
                colors.forEachIndexed { index, color ->
                    Ring(
                        color,
                        Modifier.offset(x = ThemeTokens.ringStep * (index + row / 2f), y = ThemeTokens.ringDrop * row),
                    )
                }
            }
        }
    }
}

@Composable
private fun Ring(
    color: Color,
    modifier: Modifier,
) {
    // The color sits inside a disc of the card's color, not under a border on the same edge,
    // so its antialiased rim cannot show past the outline as a light halo.
    Box(
        modifier
            .size(ThemeTokens.ringSize)
            .background(OtusColors.bgDark, CircleShape)
            .padding(Sizes.borderWidth * 2)
            .background(color, CircleShape),
    )
}

/** The raised surface, page, and controls. */
private val OtusPalette.surfaces: List<Color>
    get() = listOf(bgDark, bgRegular, bgLight)

/** The strongest text and the accent outline. */
private val OtusPalette.inks: List<Color>
    get() = listOf(text1, accentRegular)
