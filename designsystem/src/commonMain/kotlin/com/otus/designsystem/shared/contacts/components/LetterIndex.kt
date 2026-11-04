package com.otus.designsystem.shared.contacts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import com.otus.designsystem.theme.tokens.rows.LetterIndexTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * Hosts a jump-to-letter index over [content], such as a whole page with a contact list filed by
 * letter. When the [state] opens, [content] blurs and the letters slide together from their
 * headings into one column on the same edge, with their counts in a second column; letters
 * whose heading is off screen slide in from that side. The letter whose heading was pressed
 * stays level with that heading, and the others line up above and below it. When the column
 * would run past the top or bottom, it moves just enough to fit; without a known heading it is
 * centred. Tapping a letter, or lifting a finger dragged from a [letterIndexHandle], jumps to it
 * and closes the index. A tap beside the letters or Back closes it without a jump.
 */
@Composable
public fun LetterIndex(
    state: LetterIndexState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SideEffect { state.bounds.keys.retainAll(state.letters.map { it.letter }.toSet()) }
    val progress = animateFloatAsState(if (state.open) 1f else 0f, motionTween(MotionSpeed.Relaxed)).value
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state.active) {
        if (state.active != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = state.open,
        onBackCompleted = state::close,
    )
    Box(modifier.onGloballyPositioned { state.windowTop = it.positionInWindow().y }) {
        Box(Modifier.blur(LetterIndexTokens.blur * progress)) { content() }
        if (progress > 0f) Letters(state, progress)
    }
}

@Composable
private fun Letters(
    state: LetterIndexState,
    progress: Float,
) {
    val density = LocalDensity.current
    // The letters keep the headings' edge, so each one only moves up or down.
    val start = with(density) { (state.headings.values.minOfOrNull { it.left } ?: 0f).toDp() }
    val letters = state.letters
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = progress }
            .background(OtusColors.scrim)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = state::close),
    ) {
        val rowHeight = minOf(maxHeight / letters.size.coerceAtLeast(1), LetterIndexTokens.rowHeight)
        Column(Modifier.fillMaxWidth().anchoredColumn(state, rowHeight, constraints.maxHeight)) {
            letters.forEachIndexed { index, letter ->
                LetterRow(state, letter, index, rowHeight, start, progress)
            }
        }
    }
}

/**
 * One letter at its place in the column, drawn moved back toward its heading by what is left of
 * [progress], and blurred along the way most in the middle of the move.
 */
@Composable
private fun LetterRow(
    state: LetterIndexState,
    letter: IndexLetter,
    index: Int,
    rowHeight: Dp,
    start: Dp,
    progress: Float,
) {
    val active = letter.letter == state.active
    val moving = MOVE_PEAK * progress * (1f - progress)
    Box(
        Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .onGloballyPositioned { state.bounds[letter.letter] = it.boundsInWindow() }
            .clickable(onClickLabel = "Jump to ${letter.letter}", role = Role.Button) { state.pick(letter.letter) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            Modifier
                .padding(start = start)
                .graphicsLayer { translationY = (state.originOf(letter.letter, index) ?: 0f) * (1f - progress) }
                .blur(
                    LetterIndexTokens.motionBlur * moving * LetterIndexTokens.SIDEWAYS_BLUR,
                    LetterIndexTokens.motionBlur * moving,
                    BlurredEdgeTreatment.Unbounded,
                ).padding(horizontal = SidebarTokens.entryPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                letter.letter,
                Modifier.width(LetterIndexTokens.letterColumn),
                style = panelTextStyle(),
                color = if (active) OtusColors.text1 else OtusColors.text2,
                fontSize = SidebarTokens.detailSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                letter.count.toString(),
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = SidebarTokens.detailSize,
                maxLines = 1,
            )
        }
    }
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

/**
 * Places the column of letters, [rowHeight] apart, so the [state]'s anchor sits level with its
 * heading in an index [containerHeight] pixels tall. It reads the state while placing, so a new
 * anchor moves the column without recomposing it, and it is in place on the first frame.
 */
private fun Modifier.anchoredColumn(
    state: LetterIndexState,
    rowHeight: Dp,
    containerHeight: Int,
): Modifier =
    offset {
        val top =
            letterColumnTop(
                anchorCenterY = state.anchorY?.minus(state.windowTop),
                anchorIndex = state.letters.indexOfFirst { it.letter == state.anchor },
                rowHeight = rowHeight.roundToPx().toFloat(),
                count = state.letters.size,
                containerHeight = containerHeight.toFloat(),
            )
        IntOffset(0, top.roundToInt())
    }

/**
 * Where a column of [count] rows, each [rowHeight] tall, starts in a [containerHeight] tall box so
 * that the row at [anchorIndex] centres on [anchorCenterY], all in pixels from the box's top. The
 * column moves only as far as it must to stay inside the box. Without an anchor, or when the
 * column is at least as tall as the box, it is centred.
 */
internal fun letterColumnTop(
    anchorCenterY: Float?,
    anchorIndex: Int,
    rowHeight: Float,
    count: Int,
    containerHeight: Float,
): Float {
    val height = rowHeight * count
    return when {
        anchorCenterY == null || anchorIndex < 0 || height >= containerHeight -> (containerHeight - height) / 2
        else -> (anchorCenterY - (anchorIndex + HALF_ROW) * rowHeight).coerceIn(0f, containerHeight - height)
    }
}

/** `progress * (1 - progress)` peaks at a quarter, so this scales a letter's move to peak at 1. */
private const val MOVE_PEAK = 4f

/** A row's centre lies half a row below its top. */
private const val HALF_ROW = 0.5f
