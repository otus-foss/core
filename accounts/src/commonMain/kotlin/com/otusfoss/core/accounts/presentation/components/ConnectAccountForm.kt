package com.otusfoss.core.accounts.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectFailure
import com.otusfoss.core.accounts.presentation.AccountsState
import com.otusfoss.core.accounts.presentation.ConnectStatus
import com.otusfoss.core.accounts.presentation.confirmHostMessage
import com.otusfoss.core.accounts.presentation.message
import com.otusfoss.core.designsystem.shared.controls.components.ButtonPair
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.input.elements.InputField
import com.otusfoss.core.designsystem.shared.input.elements.InputKind
import com.otusfoss.core.designsystem.shared.input.elements.InputNote
import com.otusfoss.core.designsystem.shared.pages.components.PageNote

/**
 * The server, username, and password fields of [draft], then what came of the last sign-in
 * through [state]: why it failed, or the question whether to trust another host. The fields clear
 * once the account is connected.
 */
@Composable
public fun ConnectAccountFields(
    draft: ConnectDraft,
    state: AccountsState,
    service: AccountService,
) {
    val status = state.status
    LaunchedEffect(status) {
        if (status is ConnectStatus.Connected) draft.clear()
    }
    ServerInput(draft.server, { draft.server = it }, service, state::checkServer)
    InputField(
        "Username",
        draft.username,
        { draft.username = it },
        placeholder = "you@example.com",
        note =
            ConnectFailure.InvalidUsername
                .message(service)
                .takeIf { ':' in draft.username }
                ?.let(::InputNote),
        kind = InputKind.Username,
    )
    InputField(
        "Password",
        draft.password,
        { draft.password = it },
        kind = InputKind.Password,
    )
    if (status is ConnectStatus.ConfirmHost) {
        ConfirmHostPrompt(status.host, state)
    } else {
        statusText(status, service)?.let { PageNote(it) }
    }
}

/** The accent button that signs in with [draft], which waits until the draft is complete. */
@Composable
public fun ConnectButton(
    draft: ConnectDraft,
    state: AccountsState,
    modifier: Modifier = Modifier,
) {
    val connecting = state.status == ConnectStatus.Connecting
    ActionButton(
        if (connecting) "Connecting…" else "Connect",
        { draft.connect(state) },
        modifier.fillMaxWidth(),
        primary = true,
        enabled = draft.ready && !connecting,
    )
}

/** The fields with the Connect button right below them, for a sheet that has no footer of its own. */
@Composable
public fun ConnectAccountForm(
    state: AccountsState,
    service: AccountService,
) {
    val draft = rememberConnectDraft()
    ConnectAccountFields(draft, state, service)
    ConnectButton(draft, state)
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
