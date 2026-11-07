package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.overlays.elements.sheetBodyInset
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A [SheetFrame] that picks from a list: [search], such as a `PickerSearch`, stays above the
 * [content], and [footnote] explains the choice in `text2` at the end. [done] finishes the sheet,
 * such as by applying the caller's draft. With [onClear], Clear sits opposite Done, and a second
 * Clear tap within the double-tap timeout also runs [done]. The sheet scrolls the content and is
 * as tall as it; content that [scrolls] itself, such as a lazy list, gets the sheet's whole height.
 */
@Composable
public fun ListSheet(
    title: String,
    onDismiss: () -> Unit,
    done: UiAction = UiAction("Done", onClick = onDismiss),
    onClear: (() -> Unit)? = null,
    search: (@Composable () -> Unit)? = null,
    footnote: String? = null,
    scrolls: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetFrame(
        title,
        onDismiss,
        done = done,
        leadingAction = onClear?.let { clear -> { finish -> ClearAction(clear, finish) } },
    ) {
        Column(
            Modifier
                .weight(1f, fill = scrolls)
                .sheetBodyInset(),
            verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        ) {
            search?.invoke()
            Column(Modifier.weight(1f, fill = scrolls).listScroll(scrolls).selectableGroup()) {
                content()
                if (footnote != null) {
                    Text(
                        footnote,
                        Modifier.padding(horizontal = SheetTokens.gap, vertical = SheetTokens.gap),
                        color = OtusColors.text2,
                        fontSize = SheetTokens.detailSize,
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.listScroll(scrollsItself: Boolean): Modifier =
    if (scrollsItself) this else verticalScroll(rememberScrollState())

/** Clear, which clears on every tap; a quick second tap also runs [onApply]. */
@Composable
private fun ClearAction(
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    SheetHeaderAction("Clear", rememberRepeatedClear(onClear, onApply))
}

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
