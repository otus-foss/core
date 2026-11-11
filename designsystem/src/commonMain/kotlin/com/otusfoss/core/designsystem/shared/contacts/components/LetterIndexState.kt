package com.otusfoss.core.designsystem.shared.contacts.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toSize
import com.otusfoss.core.designsystem.theme.tokens.rows.LetterIndexTokens

/**
 * One letter of a [LetterIndex], how many entries are filed under it, and the [names] of its first
 * entries, which the current letter shows in place of its count.
 */
public data class IndexLetter(
    public val letter: String,
    public val count: Int,
    public val names: List<String> = emptyList(),
) {
    /**
     * What the current letter shows of [names]: the first two when the first is short, else only
     * the first, so the row stays short; null without names.
     */
    internal val preview: String?
        get() =
            names
                .take(if (names.firstOrNull().orEmpty().length > LetterIndexTokens.PREVIEW_CHARACTERS) 1 else 2)
                .joinToString(", ")
                .ifEmpty { null }
}

/** The edge a [LetterIndex] puts its letters on, so either thumb can reach them. */
public enum class LetterIndexSide {
    Start,
    End,
    ;

    public val other: LetterIndexSide get() = if (this == Start) End else Start
}

/**
 * Whether a [LetterIndex] is open, its letters, and the letter the finger is on while it drags
 * across the index. Letter headings open it with [letterIndexHandle] and report where they sit,
 * so the open index can grow out of them.
 */
@Stable
public class LetterIndexState {
    internal var open by mutableStateOf(false)
    internal var active by mutableStateOf<String?>(null)
    internal var letters by mutableStateOf(emptyList<IndexLetter>())
    internal var side by mutableStateOf(LetterIndexSide.Start)

    /** The letter whose heading opened the index, which keeps its place on screen. */
    internal var anchor by mutableStateOf<String?>(null)

    /** The letter last picked, which keeps the highlight while the index fades out. */
    internal var picked by mutableStateOf<String?>(null)
        private set

    /** The vertical centre of the [anchor]'s heading when the index opened, in window coordinates. */
    internal var anchorY by mutableStateOf<Float?>(null)

    /** The top of the [LetterIndex] in the window, so the column can line up with a heading. */
    internal var windowTop by mutableStateOf(0f)

    /** The part of the [LetterIndex] clear of the system bars, in window coordinates, once laid out. */
    internal var safeArea by mutableStateOf<Rect?>(null)

    /** Where each heading on screen sits, in window coordinates. */
    internal val headings = mutableStateMapOf<String, Rect>()

    /** Where each letter of the open index sits, in window coordinates. */
    internal val bounds = mutableStateMapOf<String, Rect>()
    internal var onPick: (String) -> Unit = {}

    /** How far the user has scrolled a column too tall for the screen, in pixels, down positive. */
    internal var shift by mutableFloatStateOf(0f)

    /** How far [shift] may go while the whole column stays reachable; set while placing the column. */
    internal var shiftLimits: ClosedFloatingPointRange<Float> = 0f..0f

    /** The dragging finger in window coordinates, or null when no finger drags across the index. */
    internal var finger: Offset? = null
        private set

    /** Whether a finger drags across the index, so the column scrolls while it holds near an end. */
    internal var dragging by mutableStateOf(false)
        private set

    /** Where the button that moves the letters to the other side sits, in window coordinates. */
    internal var switchBounds: Rect? = null

    /** Whether a dragging finger is on that button, so lifting it there switches sides. */
    internal var onSwitch by mutableStateOf(false)
        private set
    internal var onSwitchSide: () -> Unit = {}

    /** Whether a press on a heading is part of a list scroll rather than a press meant for the index. */
    internal var scrolling: () -> Boolean = { false }

    /** Marks the letter level with [position], or the nearest one past either end of the safe area. */
    internal fun hover(position: Offset) {
        val rows = bounds.entries.sortedBy { it.value.top }
        val y = safeArea?.let { position.y.coerceIn(it.top, it.bottom) } ?: position.y
        active = (rows.firstOrNull { y < it.value.bottom } ?: rows.lastOrNull())?.key
    }

    internal fun shiftBy(delta: Float) {
        shift = (shift + delta).coerceIn(shiftLimits)
    }

    /** Follows a finger dragging across the index at [position], in window coordinates. */
    internal fun drag(position: Offset) {
        finger = position
        dragging = true
        onSwitch = switchBounds?.contains(position) == true
        if (onSwitch) active = null else hover(position)
    }

    /**
     * Ends a drag where the finger lifted: on the switch button it switches sides, on a letter it
     * picks it, and elsewhere it closes the index.
     */
    internal fun release() {
        val letter = active
        when {
            onSwitch -> switchSide()
            letter != null -> pick(letter)
            else -> close()
        }
    }

    /** Moves the letters to the other side and leaves the index open on them. */
    internal fun switchSide() {
        onSwitch = false
        onSwitchSide()
    }

    internal fun endDrag() {
        finger = null
        dragging = false
    }

    /** Opens the index on [letters], keeping [anchor]'s row level with its heading when it is known. */
    internal fun open(
        letters: List<IndexLetter>,
        onPick: (String) -> Unit,
        anchor: String? = null,
    ) {
        this.letters = letters
        this.onPick = onPick
        this.anchor = anchor
        picked = null
        onSwitch = false
        anchorY = anchor?.let(headings::get)?.center?.y
        shift = 0f
        open = true
    }

    internal fun pick(letter: String) {
        picked = letter
        close()
        onPick(letter)
    }

    internal fun close() {
        open = false
        active = null
        endDrag()
    }
}

/** The letter on the highlight: under the finger, else the one just picked, else the pressed one. */
internal val LetterIndexState.highlighted: String?
    get() = active ?: picked ?: anchor

@Composable
public fun rememberLetterIndexState(): LetterIndexState = remember { LetterIndexState() }

/**
 * Whether the finger that went [down] stays put and pressed for a long press, rather than lifting,
 * moving past the touch slop, or being taken by the list's scroll first.
 */
private suspend fun AwaitPointerEventScope.awaitLongPress(down: PointerInputChange): Boolean =
    withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
        do {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull
            val moved = (change.position - down.position).getDistance() > viewConfiguration.touchSlop
        } while (change.pressed && !change.isConsumed && !moved)
    } == null

/**
 * Makes the heading of [letter] open the [state]'s index of [letters] on a press near the edge the
 * letters sit on, or a long press anywhere on it. A tap leaves the index open to pick from; holding and
 * dragging marks the letter under the finger, and lifting jumps to it, all in one touch; lifting on
 * the button that switches sides moves the letters across instead. Picking a
 * letter either way runs [onPick] with it. The open index keeps [letter] level with this heading. A
 * short press further along the heading, or any press while a [LetterIndexScrollGuard] reports a
 * scroll, passes through to the list. Screen readers get a "Show letters" action that opens the
 * index.
 */
@Composable
public fun Modifier.letterIndexHandle(
    state: LetterIndexState,
    letter: String,
    letters: List<IndexLetter>,
    onPick: (String) -> Unit,
): Modifier {
    val currentLetters by rememberUpdatedState(letters)
    val currentPick by rememberUpdatedState(onPick)
    val currentLetter by rememberUpdatedState(letter)
    val rtl by rememberUpdatedState(LocalLayoutDirection.current == LayoutDirection.Rtl)
    // Written on layout but never read while composing, so it causes no recomposition.
    val coordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }
    DisposableEffect(state, letter) { onDispose { state.headings.remove(letter) } }
    return onGloballyPositioned {
        coordinates.value = it
        state.headings[letter] = Rect(it.positionInWindow(), it.size.toSize())
    }.semantics {
        onClick(label = "Show letters") {
            state.open(currentLetters, currentPick, currentLetter)
            true
        }
    }.pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown()
            if (state.scrolling()) return@awaitEachGesture
            val fromRight = rtl != (state.side == LetterIndexSide.End)
            val fromEdge = if (fromRight) size.width - down.position.x else down.position.x
            if (fromEdge > LetterIndexTokens.handleWidth.toPx() && !awaitLongPress(down)) return@awaitEachGesture
            down.consume()
            state.open(currentLetters, currentPick, currentLetter)
            var dragging = false
            // A heading removed mid-drag cancels this without an up event, so the drag ends here.
            try {
                do {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) dragging = true
                    val layout = coordinates.value
                    if (dragging && layout != null) state.drag(layout.localToWindow(change.position))
                    change.consume()
                } while (change.pressed)
            } finally {
                state.endDrag()
            }
            if (dragging) state.release()
        }
    }
}
