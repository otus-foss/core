package com.otus.accounts.data

import com.otus.accounts.domain.AccountService
import com.otus.accounts.domain.JmapAccountDraft
import com.otus.accounts.testing.stalwartLike
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.login.InMemoryLoginStore
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountSaveCancellationTest {
    private val draft = JmapAccountDraft("https://mail.example.org/.well-known/jmap", "ada@example.org", "secret")
    private val mailboxes = """{"methodResponses": [["Mailbox/get", {"state": "m", "list": [{"id": "a"}]}, "c0"]]}"""
    private val saved = InMemoryLoginStore()
    private val changed = mutableListOf<String>()
    private lateinit var connecting: Job

    private fun repository(
        store: LoginStore,
        onProbe: () -> Unit = {},
    ): JmapAccountRepository {
        val client =
            JmapClient(
                stalwartLike { _ ->
                    onProbe()
                    mailboxes
                },
            )
        return JmapAccountRepository(client, JmapConnections(client, store), store, AccountService.Mail) {
            changed += it
        }
    }

    @Test
    fun cancellingDuringTheSaveStillFinishesItsFollowUps() =
        runTest {
            val repository = repository(CancellingLoginStore(saved) { connecting.cancel() })
            connecting = launch { repository.connect(draft) }
            connecting.join()
            assertTrue(connecting.isCancelled)
            val login = saved.logins().single()
            assertEquals(listOf(login.id), changed)
        }

    @Test
    fun cancellingBeforeTheSaveStoresNothing() =
        runTest {
            val repository = repository(saved) { connecting.cancel() }
            connecting = launch { repository.connect(draft) }
            connecting.join()
            assertTrue(connecting.isCancelled)
            assertEquals(emptyList(), saved.logins())
            assertEquals(emptyList(), changed)
        }
}

/** Cancels the caller while the platform store is writing, as a dismissed sheet would. */
private class CancellingLoginStore(
    private val store: LoginStore,
    private val cancel: () -> Unit,
) : LoginStore by store {
    override fun save(
        login: JmapLogin,
        secret: String,
    ) {
        cancel()
        store.save(login, secret)
    }
}
