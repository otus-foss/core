package com.otus.accounts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.accounts.domain.AccountService
import com.otus.accounts.domain.ConnectFailure
import com.otus.accounts.domain.JmapAccountDraft
import com.otus.accounts.domain.JmapServerAddress
import com.otus.accounts.domain.isValidUsername
import com.otus.accounts.domain.parseJmapServer
import com.otus.accounts.presentation.AccountsState
import com.otus.accounts.presentation.ConnectStatus
import com.otus.accounts.presentation.components.ConnectedAccountRow
import com.otus.accounts.presentation.confirmHostMessage
import com.otus.accounts.presentation.message
import com.otus.designsystem.components.ConfirmDialog
import com.otus.designsystem.components.PageNote
import com.otus.designsystem.components.SettingsSection
import com.otus.designsystem.components.SheetFrame
import com.otus.designsystem.components.Subpage
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.elements.InputField
import com.otus.designsystem.elements.InputKind
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens

/** Connected JMAP accounts, and a form that verifies a new one before saving it. */
@Composable
public fun AccountSetupScreen(
    state: AccountsState,
    title: String,
    service: AccountService,
    onBack: () -> Unit,
) {
    var removing by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.status) {
        if (state.status is ConnectStatus.Connected) adding = false
    }
    Subpage(title, onBack) {
        state.storageError?.let {
            PageNote(it)
            ActionButton("Reload accounts", state::reload)
        }
        SettingsSection("Connected accounts") {
            if (state.accounts.isEmpty() && state.storageError == null) {
                PageNote(if (state.loading) "Loading accounts…" else "No accounts connected yet.")
            }
            state.accounts.forEach { account -> ConnectedAccountRow(account) { removing = account.id } }
        }
        ActionButton("Connect an account", {
            state.cancelConnect()
            adding = true
        }, Modifier.fillMaxWidth(), primary = true)
    }
    if (adding) {
        SheetFrame("Connect an account", onDismiss = {
            state.cancelConnect()
            adding = false
        }) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(SheetTokens.inset),
                verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
            ) { AddAccountForm(state, service) }
        }
    }
    state.accounts.firstOrNull { it.id == removing }?.let { account ->
        ConfirmDialog(
            title = "Remove ${account.name}?",
            message = "Removes it from this app only. Your account and mail stay on your server.",
            confirmLabel = "Remove",
            onConfirm = { state.remove(account.id) },
            onDismiss = { removing = null },
        )
    }
}

@Composable
private fun AddAccountForm(
    state: AccountsState,
    service: AccountService,
) {
    var server by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    // Kept out of saved instance state on purpose.
    var password by remember { mutableStateOf("") }
    val address = parseJmapServer(server)
    val status = state.status
    LaunchedEffect(status) {
        if (status is ConnectStatus.Connected) {
            server = ""
            username = ""
            password = ""
        }
    }
    PageNote("Enter your JMAP server and sign-in details. Use an app password if your server offers one.")
    InputField("Server", server, {
        server = it
    }, placeholder = "${service.name}.example.com", supportingText = serverHint(address), kind = InputKind.Url)
    InputField(
        "Username",
        username,
        { username = it },
        placeholder = "you@example.com",
        supportingText = ConnectFailure.InvalidUsername.message(service).takeIf { ':' in username },
        kind = InputKind.Email,
    )
    InputField("Password", password, { password = it }, kind = InputKind.Password)
    val connecting = status == ConnectStatus.Connecting
    val ready = address is JmapServerAddress.Valid && isValidUsername(username.trim()) && password.isNotEmpty()
    ActionButton(
        if (connecting) "Connecting…" else "Connect",
        {
            if (address is JmapServerAddress.Valid) {
                state.connect(
                    JmapAccountDraft(address.sessionUrl, username.trim(), password),
                )
            }
        },
        Modifier.fillMaxWidth(),
        primary = true,
        enabled = ready && !connecting,
    )
    if (status is ConnectStatus.ConfirmHost) {
        ConfirmHostPrompt(status.host, state)
    } else {
        statusText(status, service)?.let {
            PageNote(it)
        }
    }
}

@Composable
private fun ConfirmHostPrompt(
    host: String,
    state: AccountsState,
) {
    PageNote(confirmHostMessage(host))
    Row(horizontalArrangement = Arrangement.spacedBy(PageTokens.rowGap)) {
        ActionButton("Cancel", state::cancelHost, Modifier.weight(1f))
        ActionButton("Trust $host", state::trustHost, Modifier.weight(1f), primary = true)
    }
}

private fun statusText(
    status: ConnectStatus,
    service: AccountService,
): String? =
    when (status) {
        ConnectStatus.Idle, ConnectStatus.Connecting -> null
        is ConnectStatus.Connected -> "Connected ${status.name}."
        is ConnectStatus.Failed -> status.reason.message(service)
        is ConnectStatus.ConfirmHost -> confirmHostMessage(status.host)
    }

private fun serverHint(address: JmapServerAddress): String =
    when (address) {
        JmapServerAddress.Empty -> "Your provider's host name, or its full JMAP session URL."
        JmapServerAddress.Insecure -> "JMAP requires a secure connection. Use https://."
        JmapServerAddress.Invalid -> "This doesn't look like a server address."
        is JmapServerAddress.Valid -> "Connects to ${address.sessionUrl}"
    }
