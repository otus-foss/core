package com.otusfoss.core.designsystem.shared.settings.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.otusfoss.core.designsystem.theme.colors.hslColor

/**
 * Picks a [hue] in degrees over a rainbow track. The track shows every hue at [saturation] and
 * [lightness], so it can preview the colors a choice gives, such as an accent's.
 */
@Composable
public fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    saturation: Float = TRACK_SATURATION,
    lightness: Float = TRACK_LIGHTNESS,
) {
    val track = remember(saturation, lightness) { hueTrack(saturation, lightness) }
    ColorSlider(hue, onHueChange, track, label, modifier, range = 0f..MAX_HUE)
}

/** Every hue at [saturation] and [lightness], from red at the left back round to red at the right. */
private fun hueTrack(
    saturation: Float,
    lightness: Float,
): Brush = Brush.horizontalGradient(List(HUE_STOPS + 1) { hslColor(MAX_HUE * it / HUE_STOPS, saturation, lightness) })

/** The largest hue: 360° is red again, the same as 0°. */
public const val MAX_HUE: Float = 359f

/** Twelve stops keep the bands smooth even at a low saturation. */
private const val HUE_STOPS = 12
private const val TRACK_SATURATION = 0.55f
private const val TRACK_LIGHTNESS = 0.45f
