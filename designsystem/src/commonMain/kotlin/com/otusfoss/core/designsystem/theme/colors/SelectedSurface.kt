package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the content sits on `selectedFill`, such as inside a selected card. Quiet outlines and
 * switch tracks are drawn in that same grey, so there they take a color that shows.
 */
public val LocalOnSelectedFill: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }
