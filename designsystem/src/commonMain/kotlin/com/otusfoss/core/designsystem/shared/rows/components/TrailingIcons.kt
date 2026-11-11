package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes

/**
 * Up to two icons at the end of a row, each centered in a touch-target-wide slot with no gap
 * between, so rows that use it line their icons up in two columns: [first], then [last] at the
 * very end. A missing [last] leaves no slot.
 */
@Composable
public fun TrailingIcons(
    first: (@Composable () -> Unit)?,
    last: (@Composable () -> Unit)?,
) {
    Row {
        listOfNotNull(first, last).forEach { icon ->
            Box(Modifier.size(Sizes.touchTarget), contentAlignment = Alignment.Center) { icon() }
        }
    }
}
