package com.otus.designsystem.shared.overlays.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.controls.components.ButtonPair
import com.otus.designsystem.shared.controls.components.PairButton
import com.otus.designsystem.theme.colors.OtusColors

/**
 * Asks before a destructive change, such as removing an account or deleting a folder. [message]
 * says what will happen and what is kept. Cancel, or [dismissLabel], Back, and the scrim change nothing;
 * [confirmLabel] closes the popup and then runs [onConfirm].
 */
@Composable
public fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Cancel",
) {
    DialogFrame(title, onDismiss, closeButton = false) {
        Text(message, color = OtusColors.text2)
        val confirm = {
            onDismiss()
            onConfirm()
        }
        ButtonPair(PairButton(dismissLabel, onDismiss), PairButton(confirmLabel, confirm))
    }
}
