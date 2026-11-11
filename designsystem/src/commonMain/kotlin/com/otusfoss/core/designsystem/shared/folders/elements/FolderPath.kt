package com.otusfoss.core.designsystem.shared.folders.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A folder's place in the tree on one line, such as "Finance / Invoices": the parents in `text3`
 * and the leaf, the folder itself, in `text1`. It never cuts a parent's name. When the whole path
 * does not fit, the parents nearest the root give way first, so the closest ones still tell same
 * named folders apart; the leaf alone shortens only when even it does not fit.
 */
@Composable
public fun FolderPath(
    path: List<String>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Layout(
        content = {
            path.dropLast(1).forEach { parent ->
                Text(
                    parent + SEPARATOR,
                    style = panelTextStyle(),
                    color = OtusColors.text3,
                    fontSize = fontSize,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Text(
                path.lastOrNull().orEmpty(),
                style = panelTextStyle(),
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val leaf = measurables.last()
        val leafWidth = leaf.maxIntrinsicWidth(constraints.maxHeight)
        val parents = measurables.dropLast(1)
        val widths = parents.map { it.maxIntrinsicWidth(constraints.maxHeight) }
        val dropped =
            parents.indices.firstOrNull { first -> widths.drop(first).sum() + leafWidth <= constraints.maxWidth }
                ?: parents.size
        val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val shown = parents.drop(dropped).map { it.measure(loose) }
        val start = shown.sumOf { it.width }
        val leafRoom = if (constraints.hasBoundedWidth) constraints.maxWidth - start else Constraints.Infinity
        val leafPlaceable = leaf.measure(loose.copy(maxWidth = leafRoom))
        val height = (shown + leafPlaceable).maxOf { it.height }
        layout((start + leafPlaceable.width).coerceAtLeast(constraints.minWidth), height) {
            (shown + leafPlaceable).fold(0) { x, piece ->
                piece.placeRelative(x, 0)
                x + piece.width
            }
        }
    }
}

internal const val SEPARATOR = " / "
