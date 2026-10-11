package com.otus.accounts.presentation.components

import androidx.compose.runtime.Composable
import com.otus.accounts.domain.ConnectedAccount
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.elements.RowButton
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_alternate_email

/** One connected account, with the address it signs in with and a button that removes it. */
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

/**
 * The lines under an account's name: its username, unless the name already is the username
 * because the server gave no display name, and then its server.
 */
internal fun connectedAccountDetail(account: ConnectedAccount): String =
    listOfNotNull(account.username.takeUnless { it.equals(account.name, ignoreCase = true) }, account.server)
        .joinToString("\n")
