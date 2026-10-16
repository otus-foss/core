package com.otus.designsystem.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.elements.ToastAction
import com.otus.designsystem.theme.ToastTokens

/**
 * A message for [ToastHost]. A new message with the same non-null [group] as one on screen
 * replaces it instead of stacking, and its timer starts over, so repeating an action can update
 * "Email deleted" to "3 emails deleted".
 */
@Immutable
public data class ToastMessage(
    val text: String,
    val action: ToastAction? = null,
    val group: Any? = null,
)

/** The toasts on screen, newest last. Create it with `remember` and pass it to [ToastHost]. */
@Stable
public class ToastState {
    internal val entries = mutableStateListOf<ToastEntry>()

    /** Whether the group is fanned out into a list. Every timer pauses while it is. */
    internal var expanded by mutableStateOf(false)

    private var nextId = 0L

    /** Shows [message] in front of the others, or replaces the toast of its group. */
    public fun show(message: ToastMessage) {
        val merged = message.group?.let(::shown)
        val entry = merged?.also { entries.remove(it) } ?: ToastEntry(nextId++, message)
        entry.message = message
        entry.elapsedMillis = 0L
        entries += entry
    }

    /** The messages on screen, newest last, leaving out toasts that are fading out. */
    public val messages: List<ToastMessage> get() = entries.filterNot { it.leaving }.map { it.message }

    /** Whether a toast of [group] is on screen, so a merged message can keep counting from it. */
    public fun isShowing(group: Any): Boolean = shown(group) != null

    private fun shown(group: Any): ToastEntry? = entries.firstOrNull { !it.leaving && it.message.group == group }

    /** Runs every timer that is not paused [deltaMillis] forward and lets expired toasts leave. */
    internal fun tick(
        deltaMillis: Long,
        timeout: (ToastMessage) -> Long,
    ) {
        if (expanded) return
        entries
            .filterNot { it.leaving || it.held }
            .forEach { entry ->
                entry.durationMillis = timeout(entry.message)
                entry.elapsedMillis += deltaMillis
                if (entry.elapsedMillis >= entry.durationMillis) entry.leaving = true
            }
    }

    /** Removes a toast once it has left the screen; a single toast left has nothing to fan out. */
    internal fun remove(entry: ToastEntry) {
        entries.remove(entry)
        if (entries.size <= 1) expanded = false
    }
}

/** One toast on screen and its timer. */
@Stable
internal class ToastEntry(
    val id: Long,
    message: ToastMessage,
) {
    var message by mutableStateOf(message)
    var elapsedMillis by mutableLongStateOf(0L)
    var durationMillis by mutableLongStateOf(baseTimeout(message))

    /** The share of its time left, which the action's ring shows. */
    val remaining: Float get() = 1f - elapsedMillis.toFloat() / durationMillis

    /** Paused while the user drags it. */
    var held by mutableStateOf(false)

    /** Its time is up and it is fading out. */
    var leaving by mutableStateOf(false)
}

/** How long [message] stays on screen before it fades, before accessibility adjustments. */
internal fun baseTimeout(message: ToastMessage): Long =
    if (message.action != null) ToastTokens.ACTION_MILLIS else ToastTokens.PLAIN_MILLIS
