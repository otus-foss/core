package com.otus.designsystem.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Indents a nested row by [depth] steps of [step], a top-level row being depth 0. Rows deeper than
 * [MAX_INDENT_STEPS] stay at that indent, so a folder nested deeply by another app keeps room for
 * its name.
 */
public fun Modifier.nestIndent(
    step: Dp,
    depth: Int,
): Modifier = padding(start = step * depth.coerceIn(0, MAX_INDENT_STEPS))

/** The deepest indent, four steps: the fifth level of a tree. */
public const val MAX_INDENT_STEPS: Int = 4
