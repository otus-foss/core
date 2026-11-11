package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.domain.JmapAccountDraft
import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.TestLifecycle
import com.otusfoss.core.accounts.testing.stalwartLike
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.login.InMemoryLoginStore
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginStore
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
        val lifecycle = TestLifecycle(onConnected = { changed += it })
        return JmapAccountRepository(client, JmapConnections(client, store), store, MailService, listOf(lifecycle))
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
