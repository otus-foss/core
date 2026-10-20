package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

/**
 * Shows the first of [options], from the widest to the shortest, that fits the width it is given
 * at its natural size, such as a folder's name, then only its count, then only its icon. The last
 * option shows when none of the others fits, squeezed to the width if it must be.
 */
@Composable
public fun FirstFit(
    options: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Layout(options.map { option -> { Box { option() } } }, modifier) { measurables, constraints ->
        val natural = Constraints(maxHeight = constraints.maxHeight)
        val last = measurables.lastIndex
        val placeables =
            measurables.mapIndexed { index, option ->
                option.single().measure(if (index == last) constraints.copy(minWidth = 0) else natural)
            }
        val shown = placeables.dropLast(1).firstOrNull { it.width <= constraints.maxWidth } ?: placeables.lastOrNull()
        layout(shown?.width ?: 0, shown?.height ?: 0) { shown?.place(0, 0) }
    }
}
