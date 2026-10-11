package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens

/**
 * Asks before a destructive change, such as removing an account or deleting a folder. [message]
 * says what will happen and what is kept. Cancel, Back, and the scrim change nothing;
 * [confirmLabel] closes the popup and then runs [onConfirm].
 */
@Composable
public fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogFrame(title, onDismiss, closeButton = false) {
        Text(message, color = OtusColors.text2)
        Row(horizontalArrangement = Arrangement.spacedBy(PageTokens.rowGap)) {
            ActionButton("Cancel", onDismiss, Modifier.weight(1f))
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
