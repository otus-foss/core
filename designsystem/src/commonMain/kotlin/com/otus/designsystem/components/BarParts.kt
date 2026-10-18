package com.otus.designsystem.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Modifiers for a bar's two parts: the [group] of buttons, and the [primary] action. A [BarMorph]
 * provides them as [LocalBarParts] to both of its bars, so each part moves into its match in the
 * other bar.
 */
@Immutable
internal class BarParts(
    val group: Modifier = Modifier,
    val primary: Modifier = Modifier,
)

/** The parts of the bar being drawn; empty outside a [BarMorph]. */
internal val LocalBarParts: ProvidableCompositionLocal<BarParts> = staticCompositionLocalOf { BarParts() }
