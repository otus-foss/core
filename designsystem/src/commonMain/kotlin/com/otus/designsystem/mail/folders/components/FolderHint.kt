package com.otus.designsystem.mail.folders.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.shared.folders.elements.FolderPath
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.rows.FolderTokens
import com.otus.designsystem.theme.tokens.rows.StripTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Folders on one line, such as "Also in Finance / Invoices · Freelancing": an optional `text3`
 * [prefix], then each name with its [FolderGlyph][com.otus.designsystem.shared.folders.elements.FolderGlyph]
 * and [FolderPath], apart by dots. It shows whole names while they fit and then a plain `text3`
 * "· +N more" from [more]; it never cuts a name. When even the first name does not fit, it drops
 * the count, then the prefix, and only then shortens that name. Nothing draws past the line's width.
 */
@Composable
public fun FolderHint(
    names: List<FolderName>,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    more: (Int) -> String = { "+$it more" },
    fontSize: TextUnit = ReadTokens.hintSize,
) {
    SubcomposeLayout(modifier.clipToBounds()) { constraints -> hintLayout(constraints, names, prefix, more, fontSize) }
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
    val gap = StripTokens.glyphGap.roundToPx()
    val start = prefix?.let { hint("prefix", it) }
    val startWidth = start?.let { it.width + gap } ?: 0
    val full =
        names.mapIndexed { index, name ->
            subcompose("name$index") { Name(name, fontSize) }.first().measure(loose)
        }
    val dot = hint("dot", DOT).width

    fun moreWidth(hidden: Int): Int = if (hidden == 0) 0 else dot + hint("more$hidden", more(hidden)).width
    val shown = shownCount(full.map { it.width }, startWidth, dot, ::moreWidth, constraints.maxWidth)
    val hidden = names.size - shown
    // A first name too wide for the line drops "+N more", then the prefix, and only then shortens.
    val single = shown == 1 && startWidth + full[0].width + moreWidth(hidden) > constraints.maxWidth
    val keepPrefix = !single || startWidth + full[0].width <= constraints.maxWidth
    val placed =
        if (single && !keepPrefix && full[0].width > constraints.maxWidth) {
            val fit = subcompose("fit") { Name(names[0], fontSize) }.first()
            listOf(fit.measure(loose.copy(maxWidth = constraints.maxWidth)))
        } else {
            full.take(shown)
        }
    val count = if (hidden > 0 && !single) listOf(hint("dot", DOT), hint("more$hidden", more(hidden))) else emptyList()
    val pieces = linePieces(start.takeIf { keepPrefix }, gap, placed, { hint("dot$it", DOT) }, count)
    return placeInLine(pieces, constraints)
}

/** The line's pieces, each with the gap after it: the [start] prefix, the [names] apart by dots, then the [count]. */
private fun linePieces(
    start: Placeable?,
    gap: Int,
    names: List<Placeable>,
    dot: (Int) -> Placeable,
    count: List<Placeable>,
): List<Pair<Placeable, Int>> =
    buildList {
        if (start != null) add(start to gap)
        names.forEachIndexed { index, name ->
            add(name to 0)
            if (index < names.lastIndex) add(dot(index) to 0)
        }
        addAll(count.map { it to 0 })
    }

/** How many whole names of [widths] fit after the prefix's [startWidth], with the count of the rest; at least one. */
private fun shownCount(
    widths: List<Int>,
    startWidth: Int,
    dot: Int,
    moreWidth: (Int) -> Int,
    maxWidth: Int,
): Int =
    (widths.size downTo 1).firstOrNull { count ->
        startWidth + widths.take(count).sum() + dot * (count - 1) + moreWidth(widths.size - count) <= maxWidth
    } ?: widths.size.coerceAtMost(1)

/** Places [pieces] side by side, each followed by its gap, centered on the line's height. */
private fun MeasureScope.placeInLine(
    pieces: List<Pair<Placeable, Int>>,
    constraints: Constraints,
): MeasureResult {
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
        horizontalArrangement = Arrangement.spacedBy(StripTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderNameGlyph(name, FolderTokens.glyphSize)
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
