package com.otus.accounts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.domain.JmapAccountDraft
import com.otus.accounts.domain.JmapServerAddress
import com.otus.accounts.domain.isValidUsername
import com.otus.accounts.domain.parseJmapServer
import com.otus.accounts.presentation.AccountAvatars
import com.otus.accounts.presentation.AccountsState
import com.otus.accounts.presentation.ConnectStatus
import com.otus.accounts.presentation.components.ConnectedAccountRow
import com.otus.accounts.presentation.components.ServerInput
import com.otus.accounts.presentation.confirmHostMessage
import com.otus.accounts.presentation.message
import com.otus.designsystem.shared.controls.components.ButtonPair
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.feedback.components.InfoText
import com.otus.designsystem.shared.input.elements.InputField
import com.otus.designsystem.shared.input.elements.InputKind
import com.otus.designsystem.shared.input.elements.InputNote
import com.otus.designsystem.shared.overlays.components.ConfirmDialog
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.pages.components.PageNote
import com.otus.designsystem.shared.pages.components.Subpage
import com.otus.designsystem.shared.settings.components.SettingsSection
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * Connected JMAP accounts with their [avatars], and a form that verifies a new one before saving
 * it. [avatarEditor] edits the avatar of one address of an account, or of all of them when the
 * address is null.
 */
@Composable
public fun AccountSetupScreen(
    state: AccountsState,
    avatars: AccountAvatars,
    avatarEditor: @Composable (account: ConnectedAccount, email: String?, onDismiss: () -> Unit) -> Unit,
    title: String,
    service: AccountService,
    onBack: () -> Unit,
) {
    var removing by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    // The account and address whose avatar is being edited; no address edits all of them.
    var editing by rememberSaveable { mutableStateOf<Pair<String, String?>?>(null) }
    LaunchedEffect(state.status) {
        if (state.status is ConnectStatus.Connected) adding = false
    }
    LaunchedEffect(state.accounts) { avatars.load(state.accounts) }
    Subpage(
        title,
        onBack,
        footer = {
            AccountsFooter {
                state.cancelConnect()
                adding = true
            }
        },
    ) {
        state.storageError?.let {
            PageNote(it)
            ActionButton("Reload accounts", state::reload)
        }
        ConnectedAccounts(
            state,
            avatars,
            onRemove = { removing = it },
            onEditAvatar = { id, email -> editing = id to email },
            onEditAll = { editing = it to null },
        )
    }
    editing?.let { (accountId, email) ->
        state.accounts.firstOrNull { it.id == accountId }?.let { avatarEditor(it, email) { editing = null } }
    }
    if (adding) {
        AddAccountSheet(state, service) {
            state.cancelConnect()
            adding = false
        }
    }
    state.accounts.firstOrNull { it.id == removing }?.let { account ->
        ConfirmDialog(
            title = "Remove ${account.name}?",
            message = "Removes it from this app only. Your account and ${service.name} stay on your server.",
            confirmLabel = "Remove",
            onConfirm = { state.remove(account.id) },
            onDismiss = { removing = null },
        )
    }
}

/** Each account with its address pills, or why there are none yet. */
@Composable
private fun ConnectedAccounts(
    state: AccountsState,
    avatars: AccountAvatars,
    onRemove: (String) -> Unit,
    onEditAvatar: (account: String, email: String) -> Unit,
    onEditAll: (account: String) -> Unit,
) {
    SettingsSection("Connected accounts", info = AvatarsInfo.takeIf { state.accounts.isNotEmpty() }) {
        if (state.accounts.isEmpty() && state.storageError == null) {
            PageNote(if (state.loading) "Loading accounts…" else "No accounts connected yet.")
        }
        state.accounts.forEach { account ->
            ConnectedAccountRow(
                account,
                avatar = { avatars.avatar(account, it) },
                accountAvatar = avatars.accountAvatar(account),
                onRemove = { onRemove(account.id) },
                onEditAvatar = { onEditAvatar(account.id, it) },
                onEditAll = { onEditAll(account.id) },
            )
        }
        avatars.error?.let { PageNote(it) }
    }
}

/** The button that connects an account. */
@Composable
private fun AccountsFooter(onConnect: () -> Unit) {
    ActionButton("Connect an account", onConnect, Modifier.fillMaxWidth(), primary = true)
}

@Composable
private fun AddAccountSheet(
    state: AccountsState,
    service: AccountService,
    onDismiss: () -> Unit,
) {
    SheetFrame("Connect an account", onDismiss = onDismiss) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        ) { AddAccountForm(state, service) }
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
    ServerInput(server, { server = it }, service, state::checkServer)
    InputField(
        "Username",
        username,
        { username = it },
        placeholder = "you@example.com",
        note =
            ConnectFailure.InvalidUsername
                .message(service)
                .takeIf { ':' in username }
                ?.let(::InputNote),
        kind = InputKind.Username,
    )
    InputField(
        "Password",
        password,
        { password = it },
        note = InputNote("Use an app password if your server offers one."),
        kind = InputKind.Password,
    )
    val connecting = status == ConnectStatus.Connecting
    val ready = address is JmapServerAddress.Valid && isValidUsername(username.trim()) && password.isNotEmpty()
    ActionButton(
        if (connecting) "Connecting…" else "Connect",
        {
            if (address is JmapServerAddress.Valid) {
                state.connect(JmapAccountDraft(address.sessionUrl, username.trim(), password))
            }
        },
        Modifier.fillMaxWidth(),
        primary = true,
        enabled = ready && !connecting,
    )
    if (status is ConnectStatus.ConfirmHost) {
        ConfirmHostPrompt(status.host, state)
    } else {
        statusText(status, service)?.let { PageNote(it) }
    }
}

@Composable
private fun ConfirmHostPrompt(
    host: String,
    state: AccountsState,
) {
    PageNote(confirmHostMessage(host))
    ButtonPair(UiAction("Cancel", onClick = state::cancelHost), UiAction("Trust $host", onClick = state::trustHost))
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

private val AvatarsInfo =
    InfoText(
        "Address avatars",
        "Otus shows it wherever it marks which address a message was sent to.",
    )
