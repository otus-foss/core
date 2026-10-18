package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.elements.FolderPath
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * Folders on one line, such as "Also in Finance / Invoices · Freelancing": an optional `text3`
 * [prefix], then each name with its [FolderGlyph] and [FolderPath], apart by dots. It shows whole
 * names while they fit and then a plain `text3` "· +N more" from [more]; it never cuts a name,
 * except the first when it alone does not fit.
 */
@Composable
public fun FolderHint(
    names: List<FolderName>,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    more: (Int) -> String = { "+$it more" },
    fontSize: TextUnit = ReadTokens.hintSize,
) {
    SubcomposeLayout(modifier) { constraints -> hintLayout(constraints, names, prefix, more, fontSize) }
}

/** Measures each piece of a [FolderHint] once, keyed by its slot, and keeps the ones that fit. */
private fun SubcomposeMeasureScope.hintLayout(
    constraints: Constraints,
    names: List<FolderName>,
    prefix: String?,
    more: (Int) -> String,
    fontSize: TextUnit,
): MeasureResult {
    val loose = Constraints(maxHeight = constraints.maxHeight)
    val measured = mutableMapOf<String, Placeable>()

    fun hint(
        slot: String,
        text: String,
    ): Placeable = measured.getOrPut(slot) { subcompose(slot) { HintText(text, fontSize) }.first().measure(loose) }
    val gap = ReadTokens.glyphGap.roundToPx()
    val start = prefix?.let { hint("prefix", it) }
    val startWidth = start?.let { it.width + gap } ?: 0
    val full =
        names.mapIndexed {
            index,
            name,
            ->
            subcompose("name$index") { Name(name, fontSize) }.first().measure(loose)
        }
    val dot = hint("dot", DOT).width

    fun moreWidth(hidden: Int): Int = if (hidden == 0) 0 else dot + hint("more$hidden", more(hidden)).width
    val shown =
        (names.size downTo 1).firstOrNull { count ->
            startWidth + full.take(count).sumOf { it.width } + dot * (count - 1) + moreWidth(names.size - count) <=
                constraints.maxWidth
        } ?: names.size.coerceAtMost(1)
    val hidden = names.size - shown
    val placed =
        if (shown == 1 && startWidth + full[0].width + moreWidth(hidden) > constraints.maxWidth) {
            val room = (constraints.maxWidth - startWidth - moreWidth(hidden)).coerceAtLeast(0)
            listOf(subcompose("fit") { Name(names[0], fontSize) }.first().measure(loose.copy(maxWidth = room)))
        } else {
            full.take(shown)
        }
    val pieces =
        buildList {
            start?.let { add(it to gap) }
            placed.forEachIndexed { index, name ->
                add(name to 0)
                if (index < placed.lastIndex) add(hint("dot$index", DOT) to 0)
            }
            if (hidden > 0) {
                add(hint("dot", DOT) to 0)
                add(hint("more$hidden", more(hidden)) to 0)
            }
        }
    val height = pieces.maxOfOrNull { it.first.height } ?: 0
    val width = pieces.sumOf { it.first.width + it.second }
    return layout(width.coerceIn(constraints.minWidth, constraints.maxWidth), height) {
        pieces.fold(0) { x, (piece, after) ->
            piece.placeRelative(x, (height - piece.height) / 2)
            x + piece.width + after
        }
    }
}

@Composable
private fun Name(
    name: FolderName,
    fontSize: TextUnit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ReadTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderNameGlyph(name, ReadTokens.folderGlyphSize)
        FolderPath(name.path, fontSize = fontSize)
    }
}

@Composable
private fun HintText(
    text: String,
    fontSize: TextUnit,
) {
    Text(text, style = panelTextStyle(), color = OtusColors.text3, fontSize = fontSize, maxLines = 1, softWrap = false)
}

private const val DOT = " · "
