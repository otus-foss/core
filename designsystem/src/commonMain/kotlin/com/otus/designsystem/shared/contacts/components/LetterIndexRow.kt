package com.otus.designsystem.shared.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.rows.LetterIndexTokens
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * One letter at its place in the column, [inset] from the [LetterIndexState.side] it sits on, with
 * the letter on the outer edge. It is drawn moved back toward its heading by what is left of
 * [progress], and blurred along the way most in the middle of the move.
 */
@Composable
internal fun LetterRow(
    state: LetterIndexState,
    letter: IndexLetter,
    index: Int,
    rowHeight: Dp,
    inset: Dp,
    progress: Float,
) {
    val current = letter.letter == state.highlighted
    val atEnd = state.side == LetterIndexSide.End
    val moving = MOVE_PEAK * progress * (1f - progress)
    Box(
        Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .onGloballyPositioned { state.bounds[letter.letter] = it.boundsInWindow() }
            .clickable(onClickLabel = "Jump to ${letter.letter}", role = Role.Button) { state.pick(letter.letter) },
        contentAlignment = if (atEnd) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(
            Modifier
                .padding(start = if (atEnd) 0.dp else inset, end = if (atEnd) inset else 0.dp)
                .padding(vertical = LetterIndexTokens.highlightInset)
                .fillMaxHeight()
                .graphicsLayer { translationY = (state.originOf(letter.letter, index) ?: 0f) * (1f - progress) }
                .blur(
                    LetterIndexTokens.motionBlur * moving * LetterIndexTokens.SIDEWAYS_BLUR,
                    LetterIndexTokens.motionBlur * moving,
                    BlurredEdgeTreatment.Unbounded,
                ).background(
                    if (current) OtusColors.selectedFill else Color.Transparent,
                    SmoothCornerShape(LetterIndexTokens.highlightRadius),
                ).padding(horizontal = LetterIndexTokens.rowPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (atEnd) LetterDetail(letter, current)
            Text(
                letter.letter,
                Modifier.width(LetterIndexTokens.letterColumn),
                style = panelTextStyle(),
                color = if (current) OtusColors.text1 else OtusColors.text2,
                fontSize = TypeScale.detail,
                fontWeight = FontWeight.SemiBold,
                textAlign = if (atEnd) TextAlign.End else TextAlign.Start,
                maxLines = 1,
            )
            if (!atEnd) LetterDetail(letter, current)
        }
    }
}

/** The names under [letter] while it is [current] and has them, else how many entries it holds. */
@Composable
private fun LetterDetail(
    letter: IndexLetter,
    current: Boolean,
) {
    Text(
        letter.preview.takeIf { current } ?: letter.count.toString(),
        style = panelTextStyle(),
        color = if (current) OtusColors.text2 else OtusColors.text3,
        fontSize = TypeScale.detail,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * How far [letter], the [index]th in the column, sits below its heading, in pixels, or null before
 * the column is laid out. A letter whose heading is off screen starts just past the edge it lies
 * beyond.
 */
private fun LetterIndexState.originOf(
    letter: String,
    index: Int,
): Float? {
    val row = bounds[letter] ?: return null
    val heading = headings[letter]
    val target = row.center.y
    val shown = letters.indexOfFirst { it.letter in headings }
    // bounds holds row, so it is never empty here.
    return when {
        heading != null -> heading.center.y - target
        shown < 0 || index < shown -> bounds.values.minOf { it.top } - row.height - target
        else -> bounds.values.maxOf { it.bottom } + row.height - target
    }
}

/** `progress * (1 - progress)` peaks at a quarter, so this scales a letter's move to peak at 1. */
private const val MOVE_PEAK = 4f
