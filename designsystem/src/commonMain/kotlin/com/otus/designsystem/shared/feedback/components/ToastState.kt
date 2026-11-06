package com.otus.designsystem.shared.feedback.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.theme.tokens.feedback.ToastTokens

/**
 * A message for [ToastHost]. A new message with the same non-null [group] as one on screen
 * replaces it instead of stacking, and its timer starts over, so repeating an action can update
 * "Email deleted" to "3 emails deleted". A [sticky] toast has no timer and stays until its action
 * runs or it is swiped away, such as a failure with Retry. [onClose] runs when the toast leaves
 * without its action, by its timer or a swipe, such as to finish what Undo would have reverted.
 * [swipeAction] runs instead of [onClose] when the user swipes the toast away, and its label
 * names the swipe for screen readers, such as Send to send a held message at once.
 * [durationMillis] sets how long it stays in place of the default, such as an Undo window the user
 * chose; screen readers may still lengthen it.
 */
@Immutable
public data class ToastMessage(
    val text: String,
    val action: UiAction? = null,
    val group: Any? = null,
    val sticky: Boolean = false,
    val onClose: (() -> Unit)? = null,
    val durationMillis: Long? = null,
    val swipeAction: UiAction? = null,
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
        if (merged != null) entries.remove(merged)
        val entry = merged ?: ToastEntry(nextId++, message)
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
            .filterNot { it.leaving || it.held || it.message.sticky }
            .forEach { entry ->
                entry.durationMillis = timeout(entry.message)
                entry.elapsedMillis += deltaMillis
                if (entry.elapsedMillis >= entry.durationMillis) leave(entry)
            }
    }

    /** Starts [entry] leaving, as when its time is up; one whose action [acted] skips its close callback. */
    internal fun leave(
        entry: ToastEntry,
        acted: Boolean = false,
    ) {
        entry.acted = acted
        entry.leaving = true
    }

    /**
     * Removes a toast once it has left the screen; a single toast left has nothing to fan out. One
     * that leaves without its action having [acted] runs its close callback.
     */
    internal fun remove(
        entry: ToastEntry,
        acted: Boolean = false,
    ) = drop(entry) { if (!acted) entry.message.onClose?.invoke() }

    /** Removes a toast the user swiped away: its swipe action runs, or else its close callback. */
    internal fun swipe(entry: ToastEntry) =
        drop(entry) { (entry.message.swipeAction?.onClick ?: entry.message.onClose)?.invoke() }

    private fun drop(
        entry: ToastEntry,
        then: () -> Unit,
    ) {
        if (!entries.remove(entry)) return
        then()
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

    /** Its time is up or its action ran, and it is leaving. */
    var leaving by mutableStateOf(false)

    /** It leaves because its action ran. */
    var acted = false
}

/** How long [message] stays on screen before it fades, before accessibility adjustments. */
internal fun baseTimeout(message: ToastMessage): Long =
    message.durationMillis ?: if (message.action != null) ToastTokens.ACTION_MILLIS else ToastTokens.PLAIN_MILLIS
