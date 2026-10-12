package com.otus.accounts.presentation

import com.otus.accounts.domain.AccountRepository
import com.otus.accounts.domain.AccountStorageException
import com.otus.accounts.domain.ConnectOutcome
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.domain.JmapAccountDraft
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsStateTest {
    @Test
    fun dismissingAnUnfinishedConnectionCancelsItAndAllowsAnotherAttempt() =
        runTest {
            val repository = AccountStateRepository()
            val state = AccountsState(repository, this)
            runCurrent()
            assertFalse(state.loading)
            val pending = CompletableDeferred<ConnectOutcome>()
            repository.pendingConnection = pending
            val draft = JmapAccountDraft("https://example.org/jmap", "ada", "secret")
            state.connect(draft)
            runCurrent()
            assertEquals(ConnectStatus.Connecting, state.status)
            state.cancelConnect()
            runCurrent()
            pending.complete(ConnectOutcome.Connected(repository.account))
            runCurrent()
            assertEquals(ConnectStatus.Idle, state.status)
            repository.pendingConnection = null
            state.connect(draft)
            runCurrent()
            assertEquals(ConnectStatus.Connected("Ada"), state.status)
        }

    @Test
    fun dismissingDuringASaveShowsTheAccountItStored() =
        runTest {
            val repository = AccountStateRepository()
            val state = AccountsState(repository, this)
            runCurrent()
            val pending = CompletableDeferred<ConnectOutcome>()
            repository.pendingSave = pending
            state.connect(JmapAccountDraft("https://example.org/jmap", "grace", "secret"))
            runCurrent()
            state.cancelConnect()
            runCurrent()
            pending.complete(ConnectOutcome.Connected(repository.newAccount))
            runCurrent()
            assertEquals(ConnectStatus.Idle, state.status)
            assertEquals(listOf(repository.account, repository.newAccount), state.accounts)
        }

    @Test
    fun cancelledLoadDoesNotEndTheLoadThatReplacedIt() =
        runTest {
            val repository = AccountStateRepository()
            repository.pendingRead = CompletableDeferred()
            val state = AccountsState(repository, this)
            runCurrent()
            state.reload()
            runCurrent()
            assertTrue(state.loading)
            repository.pendingRead?.complete(Unit)
            runCurrent()
            assertFalse(state.loading)
        }

    @Test
    fun dismissingHostConsentDropsThePendingCredentials() =
        runTest {
            val repository = AccountStateRepository()
            repository.pendingConnection =
                CompletableDeferred(
                    ConnectOutcome.ConfirmHost("other.example", "https://other.example"),
                )
            val state = AccountsState(repository, this)
            assertTrue(state.loading)
            state.connect(JmapAccountDraft("https://example.org/jmap", "ada", "secret"))
            runCurrent()
            assertEquals(ConnectStatus.ConfirmHost("other.example"), state.status)
            state.cancelConnect()
            state.trustHost()
            runCurrent()
            assertEquals(ConnectStatus.Idle, state.status)
        }

    @Test
    fun failedInitialLoadCanBeRetried() =
        runTest {
            val repository = AccountStateRepository().apply { failRead = true }
            val state = AccountsState(repository, this)
            runCurrent()
            assertNotNull(state.storageError)
            repository.failRead = false
            state.reload()
            runCurrent()
            assertEquals(listOf(repository.account), state.accounts)
            assertNull(state.storageError)
        }

    @Test
    fun failedRemovalKeepsAccountsAndDoesNotRefreshInboxUntilRetrySucceeds() =
        runTest {
            val repository = AccountStateRepository()
            var changes = 0
            val state = AccountsState(repository, this) { changes++ }
            runCurrent()
            repository.failRemove = true
            state.remove("a")
            runCurrent()
            assertEquals(listOf(repository.account), state.accounts)
            assertNotNull(state.storageError)
            assertEquals(0, changes)
            repository.failRemove = false
            state.remove("a")
            runCurrent()
            assertEquals(emptyList(), state.accounts)
            assertNull(state.storageError)
            assertEquals(1, changes)
        }

    @Test
    fun failedReloadPreservesPreviouslyLoadedAccounts() =
        runTest {
            val repository = AccountStateRepository()
            val state = AccountsState(repository, this)
            runCurrent()
            repository.failRead = true
            state.reload()
            runCurrent()
            assertEquals(listOf(repository.account), state.accounts)
            assertNotNull(state.storageError)
        }
}

private class AccountStateRepository : AccountRepository {
    val account = ConnectedAccount("a", "Ada", "ada", "example.org")
    var failRead = false
    var failRemove = false
    val newAccount = ConnectedAccount("b", "Grace", "grace", "example.org")
    var pendingConnection: CompletableDeferred<ConnectOutcome>? = null
    var pendingSave: CompletableDeferred<ConnectOutcome>? = null
    var pendingRead: CompletableDeferred<Unit>? = null
    private var saved = listOf(account)

    override suspend fun accounts(): List<ConnectedAccount> {
        pendingRead?.await()
        if (failRead) throw AccountStorageException(IllegalStateException("read failed"))
        return saved
    }

    override suspend fun connect(draft: JmapAccountDraft): ConnectOutcome {
        // Like JmapAccountRepository, a started save finishes even when the caller is cancelled.
        pendingSave?.let { save ->
            return withContext(NonCancellable) { save.await().also { saved = saved + newAccount } }
        }
        return pendingConnection?.await() ?: ConnectOutcome.Connected(account)
    }

    override suspend fun remove(id: String) {
        if (failRemove) throw AccountStorageException(IllegalStateException("remove failed"))
        saved = emptyList()
    }
}
