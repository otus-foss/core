package com.otus.designsystem.shared.controls.elements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.TextStyle
import com.otus.designsystem.theme.motion.LocalMotionScale

/**
 * A one-line label that rolls only what changed, as [AnimatedText] does: in a number, each digit
 * that changed slides up when the number grew and down when it shrank, and a digit gained or lost
 * at the front rolls in or out while the words after it ease to their new place. A changed word
 * rolls whole. Unchanged digits and words stay still.
 */
@Composable
public fun RollingWords(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val direction = rememberRollDirection(text)
    val words = text.split(' ')
    Row(modifier.clipToBounds()) {
        words.forEachIndexed { index, word ->
            key(index) {
                if (word.isNotEmpty() && word.all(Char::isDigit)) {
                    // Digits line up from the end, so 12 to 15 rolls only the 5; empty places hold no width.
                    val places = word.padStart(DIGIT_PLACES, ' ')
                    places.forEachIndexed { place, digit ->
                        key(places.length - place) { RollingCell(digit.toString().trim(), direction, style) }
                    }
                } else {
                    RollingCell(word, direction, style)
                }
            }
            if (index < words.lastIndex) Text(" ", style = style)
        }
    }
}

@Composable
private fun RollingCell(
    text: String,
    direction: Int,
    style: TextStyle,
) {
    val motionScale = LocalMotionScale.current
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            roll(direction, motionScale) using SizeTransform(clip = false) { _, _ -> textEnterSpec(motionScale) }
        },
        label = "Rolling text",
    ) { Text(it, style = style, maxLines = 1, softWrap = false) }
}

/** Down when [text]'s first number is lower than the last text's, up otherwise. */
@Composable
private fun rememberRollDirection(text: String): Int {
    val last = remember { LastText(text) }
    val old = last.text.firstNumber()
    val new = text.firstNumber()
    if (text != last.text) {
        last.direction = if (old != null && new != null && new < old) -1 else 1
        last.text = text
    }
    return last.direction
}

/** Not snapshot state: it only remembers the text the last composition rolled from. */
private class LastText(
    var text: String,
) {
    var direction = 1
}

private fun String.firstNumber(): Long? =
    split(' ')
        .firstOrNull { it.isNotEmpty() && it.all(Char::isDigit) }
        ?.toLongOrNull()

/** Places kept for a number's digits, so a digit gained at the front rolls in instead of popping. */
private const val DIGIT_PLACES = 6
