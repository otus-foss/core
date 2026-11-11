package com.otusfoss.core.accounts.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otusfoss.core.accounts.domain.AccountRepository
import com.otusfoss.core.accounts.domain.AccountStorageException
import com.otusfoss.core.accounts.domain.ConnectFailure
import com.otusfoss.core.accounts.domain.ConnectOutcome
import com.otusfoss.core.accounts.domain.ConnectedAccount
import com.otusfoss.core.accounts.domain.JmapAccountDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

public sealed interface ConnectStatus {
    public data object Idle : ConnectStatus

    public data object Connecting : ConnectStatus

    public data class Connected(
        val name: String,
    ) : ConnectStatus

    public data class Failed(
        val reason: ConnectFailure,
    ) : ConnectStatus

    /** Sign-in would go to [host]; the user decides whether to trust it. */
    public data class ConfirmHost(
        val host: String,
    ) : ConnectStatus
}

/** Screen state for connected accounts. [onChange] runs after an account is added or removed. */
public class AccountsState(
    private val repository: AccountRepository,
    private val scope: CoroutineScope,
    private val onChange: () -> Unit = {},
) {
    public var accounts: List<ConnectedAccount> by mutableStateOf(emptyList())
        private set
    public var status: ConnectStatus by mutableStateOf(ConnectStatus.Idle)
        private set

    public var storageError: String? by mutableStateOf(null)
        private set
    public var loading: Boolean by mutableStateOf(true)
        private set

    /** Held in memory only while the user decides about another host. */
    private var pending: Pair<JmapAccountDraft, String>? = null
    private var connecting: Job? = null
    private var loadingAccounts: Job? = null
    private var loadGeneration = 0

    init {
        reload()
    }

    public fun connect(draft: JmapAccountDraft) {
        if (status == ConnectStatus.Connecting) return
        status = ConnectStatus.Connecting
        pending = null
        connecting =
            scope.launch {
                val outcome = repository.connect(draft)
                // A save that started finishes even after the sheet closed, so show the stored account.
                if (outcome is ConnectOutcome.Connected) {
                    onChange()
                    reload()
                }
                ensureActive()
                if (outcome is ConnectOutcome.ConfirmHost) pending = draft to outcome.origin
                status =
                    when (outcome) {
                        is ConnectOutcome.Connected -> ConnectStatus.Connected(outcome.account.name)
                        is ConnectOutcome.Failed -> ConnectStatus.Failed(outcome.reason)
                        is ConnectOutcome.ConfirmHost -> ConnectStatus.ConfirmHost(outcome.host)
                    }
            }
    }

    /** Why [sessionUrl] can't be a JMAP server, or null when it can. */
    public suspend fun checkServer(sessionUrl: String): ConnectFailure? = repository.checkServer(sessionUrl)

    /** Retries the pending sign-in, trusting only the host the user just approved. */
    public fun trustHost() {
        val (draft, origin) = pending ?: return
        connect(draft.copy(trustedOrigins = draft.trustedOrigins + origin))
    }

    public fun cancelHost() {
        cancelConnect()
    }

    /** Drops pending credentials and cancels a connection when its sheet closes. */
    public fun cancelConnect() {
        connecting?.cancel()
        connecting = null
        pending = null
        status = ConnectStatus.Idle
    }

    public fun reload() {
        loadingAccounts?.cancel()
        val generation = ++loadGeneration
        loadingAccounts = scope.launch { loadAccounts(generation) }
    }

    private suspend fun loadAccounts(generation: Int) {
        loading = true
        try {
            accounts = repository.accounts()
            storageError = null
        } catch (expected: AccountStorageException) {
            storageError = "Couldn't load saved accounts. Try again."
        } finally {
            // A cancelled load finishes after its replacement started; only the latest load ends loading.
            if (generation == loadGeneration) loading = false
        }
    }

    public fun remove(id: String) {
        scope.launch {
            try {
                repository.remove(id)
                onChange()
                reload()
            } catch (expected: AccountStorageException) {
                storageError = "Couldn't remove the account. Try removing it again."
            }
        }
    }
}
