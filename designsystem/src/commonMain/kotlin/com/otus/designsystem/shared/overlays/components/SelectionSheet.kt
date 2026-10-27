package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.window.DialogProperties
import com.otus.designsystem.theme.SheetTokens
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A [SheetFrame] for picking filter values. The caller owns drafts. Done applies them through
 * [onApply]; the scrim, Back, and swiping down discard them. Clear sits opposite Done, and a second
 * Clear tap within the double-tap timeout also applies the cleared draft.
 */
@Composable
public fun SelectionSheet(
    title: String,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    applyEnabled: Boolean = true,
    search: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetFrame(
        title,
        onDismiss,
        onDone = onApply,
        doneEnabled = applyEnabled,
        leadingAction = { done -> SheetHeaderAction("Clear", rememberRepeatedClear(onClear, done)) },
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        ) {
            search?.invoke()
            Column(Modifier.weight(1f), content = content)
        }
    }
}

/** Clears on every tap; a quick second tap also applies the cleared draft. */
@Composable
private fun rememberRepeatedClear(
    onClear: () -> Unit,
    onApply: () -> Unit,
): () -> Unit {
    val timeout = LocalViewConfiguration.current.doubleTapTimeoutMillis.milliseconds
    var lastClear by remember { mutableStateOf<TimeMark?>(null) }
    return {
        onClear()
        if (lastClear?.let { it.elapsedNow() <= timeout } == true) {
            lastClear = null
            onApply()
        } else {
            lastClear = TimeSource.Monotonic.markNow()
        }
    }
}

/** Android's window-inset option is absent from the iOS dialog API. */
internal expect fun selectionSheetDialogProperties(): DialogProperties
