package com.otus.designsystem.shared.overlays.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** What a confirm dialog asks before an action that cannot be undone, and its button's [label]. */
public class ConfirmQuestion(
    public val title: String,
    public val message: String,
    public val label: String,
)

/**
 * The confirm dialog of a bar or menu, for actions that cannot be undone, such as deleting for good.
 * Show its [Dialog] once beside the actions that use it.
 */
public class Confirmation internal constructor() {
    private var pending by mutableStateOf<Pair<ConfirmQuestion, () -> Unit>?>(null)

    /** Runs [perform] now without a [question], or once the user confirms the [question]. */
    public fun run(
        question: ConfirmQuestion?,
        perform: () -> Unit,
    ) {
        if (question == null) perform() else pending = question to perform
    }

    @Composable
    public fun Dialog() {
        val (question, perform) = pending ?: return
        ConfirmDialog(
            question.title,
            question.message,
            question.label,
            onConfirm = perform,
            onDismiss = { pending = null },
        )
    }
}

@Composable
public fun rememberConfirmation(): Confirmation = remember { Confirmation() }
