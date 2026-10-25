package com.otus.designsystem.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize

/** One letter of a [LetterIndex] and how many entries are filed under it. */
public data class IndexLetter(
    public val letter: String,
    public val count: Int,
)

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

    /** Where each heading on screen sits, in window coordinates. */
    internal val headings = mutableStateMapOf<String, Rect>()

    /** Where each letter of the open index sits, in window coordinates. */
    internal val bounds = mutableStateMapOf<String, Rect>()
    internal var onPick: (String) -> Unit = {}

    /** Marks the letter level with [position], or the nearest one past either end. */
    internal fun hover(position: Offset) {
        val rows = bounds.entries.sortedBy { it.value.top }
        active =
            (rows.firstOrNull { position.y < it.value.bottom } ?: rows.lastOrNull())?.key
    }

    internal fun pick(letter: String) {
        close()
        onPick(letter)
    }

    internal fun close() {
        open = false
        active = null
    }
}

@Composable
public fun rememberLetterIndexState(): LetterIndexState = remember { LetterIndexState() }

/**
 * Makes the heading of [letter] open the [state]'s index of [letters] on press. A tap leaves the
 * index open to pick from; holding and dragging marks the letter under the finger, and lifting
 * jumps to it, all in one touch. Picking a letter either way runs [onPick] with it.
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
    // Written on layout but never read while composing, so it causes no recomposition.
    val coordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }
    DisposableEffect(state, letter) { onDispose { state.headings.remove(letter) } }
    return onGloballyPositioned {
        coordinates.value = it
        state.headings[letter] = Rect(it.positionInWindow(), it.size.toSize())
    }.pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            state.letters = currentLetters
            state.onPick = currentPick
            state.open = true
            var dragging = false
            do {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) dragging = true
                if (dragging) coordinates.value?.let { state.hover(it.localToWindow(change.position)) }
                change.consume()
            } while (change.pressed)
            if (dragging) state.active?.let(state::pick) ?: state.close()
        }
    }
}
