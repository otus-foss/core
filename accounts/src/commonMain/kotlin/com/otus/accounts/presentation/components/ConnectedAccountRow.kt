package com.otus.accounts.presentation.components

import androidx.compose.runtime.Composable
import com.otus.accounts.domain.ConnectedAccount
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.elements.RowButton
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_alternate_email

/** One connected account, its sign-in and sending addresses, and a button that removes it. */
@Composable
public fun ConnectedAccountRow(
    account: ConnectedAccount,
    onRemove: () -> Unit,
) {
    SettingRow(
        account.name,
        onClick = null,
        description = connectedAccountDetail(account),
        icon = Res.drawable.symbol_alternate_email,
    ) { RowButton("Remove", onRemove, accessibilityLabel = "Remove ${account.name}") }
}

/** Avoid repeating addresses already used as the account name or sign-in. */
internal fun connectedAccountDetail(account: ConnectedAccount): String =
    (listOf(account.username) + account.emailAddresses)
        .filterNot { it.equals(account.name, ignoreCase = true) }
        .distinctBy { it.lowercase() }
        .plus(account.server)
        .joinToString("\n")
