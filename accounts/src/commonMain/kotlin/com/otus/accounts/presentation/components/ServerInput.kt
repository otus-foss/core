package com.otus.accounts.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otus.accounts.domain.AccountService
import com.otus.accounts.domain.ConnectFailure
import com.otus.accounts.domain.JmapServerAddress
import com.otus.accounts.domain.parseJmapServer
import com.otus.accounts.presentation.message
import com.otus.designsystem.shared.input.elements.InputField
import com.otus.designsystem.shared.input.elements.InputKind
import com.otus.designsystem.shared.input.elements.InputNote
import kotlinx.coroutines.delay

/**
 * The server field. A moment after the last keystroke it asks the server, through [check], whether
 * JMAP answers there, and marks the field with a check or ✕.
 */
@Composable
internal fun ServerInput(
    value: String,
    onValueChange: (String) -> Unit,
    service: AccountService,
    check: suspend (sessionUrl: String) -> ConnectFailure?,
) {
    val address = parseJmapServer(value)
    // Starts empty, so a restored address is not marked before it has been checked.
    var checked by remember { mutableStateOf(ServerCheck(JmapServerAddress.Empty)) }
    LaunchedEffect(address) {
        delay(SERVER_CHECK_DELAY_MILLIS)
        checked = ServerCheck(address, checking = address is JmapServerAddress.Valid)
        if (address is JmapServerAddress.Valid) {
            checked = ServerCheck(address, failure = check(address.sessionUrl), answered = true)
        }
    }
    // Until the check catches up with the typing, the hint describes what is typed and nothing is marked.
    val current = checked.takeIf { it.address == address }
    InputField(
        "Server",
        value,
        onValueChange,
        placeholder = "${service.name}.example.com",
        note = InputNote((current ?: ServerCheck(address)).hint(service), valid = current?.mark()),
        kind = InputKind.Url,
    )
}

/**
 * What the field last learned about [address]: still [checking], [answered] by a JMAP server, or
 * why none answers.
 */
private data class ServerCheck(
    val address: JmapServerAddress,
    val checking: Boolean = false,
    val failure: ConnectFailure? = null,
    val answered: Boolean = false,
)

private fun ServerCheck.mark(): Boolean? =
    when {
        checking || address == JmapServerAddress.Empty -> null
        else -> address is JmapServerAddress.Valid && failure == null
    }

private fun ServerCheck.hint(service: AccountService): String =
    when {
        checking -> "Checking server…"
        failure != null -> failure.message(service)
        answered && address is JmapServerAddress.Valid -> "Found a JMAP server at ${address.sessionUrl}"
        else -> serverHint(address)
    }

private fun serverHint(address: JmapServerAddress): String =
    when (address) {
        JmapServerAddress.Empty -> "Your provider's host name, or its full JMAP session URL."
        JmapServerAddress.Insecure -> "JMAP requires a secure connection. Use https://."
        JmapServerAddress.Invalid -> "This doesn't look like a server address."
        is JmapServerAddress.Valid -> "Connects to ${address.sessionUrl}"
    }

private const val SERVER_CHECK_DELAY_MILLIS = 500L
