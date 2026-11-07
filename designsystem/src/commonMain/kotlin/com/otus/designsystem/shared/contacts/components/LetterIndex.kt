package com.otus.designsystem.shared.contacts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_swap_horiz
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.rows.LetterIndexTokens
import kotlin.math.roundToInt

/**
 * Hosts a jump-to-letter index over [content], such as a whole page with a contact list filed by
 * letter. When the [state] opens, [content] blurs and the letters slide together from their
 * headings into one column on the [side] the user picked, with their counts beside them; letters
 * whose heading is off screen slide in from that side. A button in the other top corner moves the
 * column across through [onSideChange]. The letter whose heading was pressed
 * stays level with that heading, and the others line up above and below it; without a known
 * heading the column is centred between the bars. The current letter, under the finger or else
 * the one pressed, sits on a highlight. Letters past the top or bottom, or under the status or
 * navigation bar, scroll into view by dragging the column or by holding a dragging finger near the
 * top or bottom of the thumb's reach around the pressed heading, which they scroll into. Tapping
 * a letter, or lifting a finger dragged from a [letterIndexHandle], jumps to it and closes the
 * index. A tap beside the letters or Back closes it without a jump.
 */
@Composable
public fun LetterIndex(
    state: LetterIndexState,
    side: LetterIndexSide,
    onSideChange: (LetterIndexSide) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SideEffect {
        state.bounds.keys.retainAll(state.letters.map { it.letter }.toSet())
        state.side = side
        state.onSwitchSide = { onSideChange(side.other) }
    }
    val progress = animateFloatAsState(if (state.open) 1f else 0f, motionTween(MotionSpeed.Relaxed)).value
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state.open) {
        if (state.open) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    LaunchedEffect(state.active, state.onSwitch) {
        if (state.active != null || state.onSwitch) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = state.open,
        onBackCompleted = state::close,
    )
    Box(modifier.onGloballyPositioned { state.windowTop = it.positionInWindow().y }) {
        Box(Modifier.blur(LetterIndexTokens.blur * progress)) { content() }
        Box(Modifier.matchParentSize().safeArea(state))
        if (progress > 0f) Letters(state, progress)
    }
}

@Composable
private fun Letters(
    state: LetterIndexState,
    progress: Float,
) {
    val density = LocalDensity.current
    // The page's side insets match, so the column sits as far in as the headings on either side.
    val inset = with(density) { (state.headings.values.minOfOrNull { it.left } ?: 0f).toDp() }
    val letters = state.letters
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = progress }
            .background(OtusColors.scrim)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = state::close),
    ) {
        val rowHeight = LetterIndexTokens.rowHeight
        val rowPixels = with(density) { rowHeight.toPx() }
        val containerHeight = constraints.maxHeight
        LaunchedEffect(state.dragging, rowPixels) { state.scrollAtEdges(rowPixels) }
        Box(Modifier.fillMaxSize().clipToSafeArea(state)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .anchoredColumn(state, rowHeight, containerHeight)
                    .wrapContentHeight(Alignment.Top, unbounded = true)
                    .draggable(rememberDraggableState(state::shiftBy), Orientation.Vertical)
                    .scrollSemantics(state),
            ) {
                letters.forEachIndexed { index, letter ->
                    LetterRow(state, letter, index, rowHeight, inset, progress)
                }
            }
        }
        SwitchSideButton(
            state,
            Modifier
                .align(if (state.side == LetterIndexSide.End) Alignment.TopStart else Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(horizontal = inset),
        )
    }
}

/** Moves the letters to the other side on a tap, or when a dragging finger lifts on it, which grows it. */
@Composable
private fun SwitchSideButton(
    state: LetterIndexState,
    modifier: Modifier,
) {
    val scale =
        animateFloatAsState(
            if (state.onSwitch) LetterIndexTokens.SWITCH_HOVER_SCALE else 1f,
            motionTween(MotionSpeed.Quick),
        ).value
    IconButton(
        "Switch sides",
        state::switchSide,
        modifier
            .onGloballyPositioned { state.switchBounds = it.boundsInWindow() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) { Symbol(Res.drawable.symbol_swap_horiz) }
}

/**
 * Reports the part of the index clear of the status and navigation bars and display cutouts, in
 * window coordinates. It is always laid out, so the open index knows it on its first frame.
 */
private fun Modifier.safeArea(state: LetterIndexState): Modifier =
    safeDrawingPadding().onGloballyPositioned { state.safeArea = it.boundsInWindow() }

/**
 * Places the column of letters, [rowHeight] apart, so the [state]'s anchor sits level with its
 * heading in an index [containerHeight] pixels tall, inside the index's safe area. It reads the
 * state while placing, so a new anchor moves the column without recomposing it, and it is in place
 * on the first frame.
 */
private fun Modifier.anchoredColumn(
    state: LetterIndexState,
    rowHeight: Dp,
    containerHeight: Int,
): Modifier =
    offset {
        val row = rowHeight.roundToPx().toFloat()
        val band = state.safeBand(containerHeight.toFloat())
        val top =
            letterColumnTop(
                anchorCenterY = state.anchorY?.minus(state.windowTop),
                anchorIndex = state.letters.indexOfFirst { it.letter == state.anchor },
                rowHeight = row,
                count = state.letters.size,
                band = band,
            )
        val thumb = thumbBand(band, state.anchorY?.minus(state.windowTop), row)
        val reach = scrollReach(top, row * state.letters.size, thumb)
        state.shiftLimits = (reach.start - top)..(reach.endInclusive - top)
        IntOffset(0, (top + state.shift.coerceIn(state.shiftLimits)).roundToInt())
    }

/** The safe area's span within an index [containerHeight] pixels tall, in pixels from its top. */
private fun LetterIndexState.safeBand(containerHeight: Float): ClosedFloatingPointRange<Float> {
    val safe = safeArea ?: return 0f..containerHeight
    return (safe.top - windowTop).coerceAtLeast(0f)..(safe.bottom - windowTop).coerceAtMost(containerHeight)
}

/**
 * Where a column of [count] rows, each [rowHeight] tall, starts so that the row at [anchorIndex]
 * centres on [anchorCenterY], all in pixels from the top of the index. The anchor never moves,
 * even when rows then lie past [band], the part of the index the column may show; [scrollReach]
 * brings them into view. Without an anchor, a column that fits is centred on the band and a
 * taller one starts at its top.
 */
internal fun letterColumnTop(
    anchorCenterY: Float?,
    anchorIndex: Int,
    rowHeight: Float,
    count: Int,
    band: ClosedFloatingPointRange<Float>,
): Float {
    val height = rowHeight * count
    val room = band.endInclusive - band.start
    return when {
        anchorCenterY != null && anchorIndex >= 0 -> anchorCenterY - (anchorIndex + HALF_ROW) * rowHeight
        height < room -> band.start + (room - height) / 2
        else -> band.start
    }
}

/**
 * The tops a column [height] pixels tall, placed at [top], may scroll to: far enough that its first
 * row reaches the start of [band] and its last row reaches the end, and no further than where it
 * already is. Pass the [thumbBand], so every letter can come within the thumb's reach.
 */
internal fun scrollReach(
    top: Float,
    height: Float,
    band: ClosedFloatingPointRange<Float>,
): ClosedFloatingPointRange<Float> = minOf(top, band.endInclusive - height)..maxOf(top, band.start)

/**
 * The part of [band] a thumb reaches from [pressY], where it pressed a heading: a few [rowHeight]s
 * up and down. Without a press, the whole [band].
 */
internal fun thumbBand(
    band: ClosedFloatingPointRange<Float>,
    pressY: Float?,
    rowHeight: Float,
): ClosedFloatingPointRange<Float> {
    if (pressY == null) return band
    val reach = rowHeight * LetterIndexTokens.THUMB_REACH_ROWS
    return (pressY - reach).coerceIn(band)..(pressY + reach).coerceIn(band)
}

/** A row's centre lies half a row below its top. */
private const val HALF_ROW = 0.5f
