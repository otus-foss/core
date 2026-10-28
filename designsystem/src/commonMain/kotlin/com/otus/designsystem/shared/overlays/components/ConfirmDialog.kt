package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PageTokens

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
        Row(horizontalArrangement = Arrangement.spacedBy(PageTokens.rowGap)) {
            ActionButton(dismissLabel, onDismiss, Modifier.weight(1f))
            ActionButton(
                confirmLabel,
                {
                    onDismiss()
                    onConfirm()
                },
                Modifier.weight(1f),
                primary = true,
            )
        }
    }
}
