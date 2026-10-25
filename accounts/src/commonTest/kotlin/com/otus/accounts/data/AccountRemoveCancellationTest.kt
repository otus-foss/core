package com.otus.accounts.data

import com.otus.accounts.domain.AccountService
import com.otus.accounts.testing.stalwartLike
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.login.InMemoryLoginStore
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AccountRemoveCancellationTest {
    private val login = JmapLogin("ada", "https://mail.example.org/jmap", "ada@example.org", "Ada")
    private val saved = InMemoryLoginStore().apply { save(login, "secret") }
    private val client = JmapClient(stalwartLike { error("Removal needs no server request") })
    private lateinit var removing: Job

    @Test
    fun cancellationDuringCredentialDeletionStillInvalidatesTheConnectionAndNotifies() =
        runTest {
            val store =
                object : LoginStore by saved {
                    override fun remove(id: String) {
                        saved.remove(id)
                        removing.cancel()
                    }
                }
            val connections = JmapConnections(client, store)
            connections.connection(login)
            val changed = mutableListOf<String>()
            val repository =
                JmapAccountRepository(
                    client,
                    connections,
                    store,
                    AccountService.Mail,
                    onAccountChanged = { changed += it },
                )

            removing = launch { repository.remove(login.id) }
            removing.join()

            assertTrue(removing.isCancelled)
            assertTrue(saved.logins().isEmpty())
            assertEquals(listOf(login.id), changed)
            assertFailsWith<JmapException.CredentialsUnavailable> { connections.connection(login) }
        }

    @Test
    fun cancellationDuringTheAccountCallbackDoesNotInterruptItsCleanup() =
        runTest {
            val connections = JmapConnections(client, saved)
            connections.connection(login)
            var cleaned = false
            val repository =
                JmapAccountRepository(
                    client,
                    connections,
                    saved,
                    AccountService.Mail,
                    onAccountChanged = {
                        removing.cancel()
                        yield()
                        cleaned = true
                    },
                )

            removing = launch { repository.remove(login.id) }
            removing.join()

            assertTrue(removing.isCancelled)
            assertTrue(cleaned)
            assertTrue(saved.logins().isEmpty())
            assertFailsWith<JmapException.CredentialsUnavailable> { connections.connection(login) }
        }

    @Test
    fun cancellationBeforeTheCommitLeavesTheLoginAvailable() =
        runTest {
            var changed = false
            val repository =
                JmapAccountRepository(
                    client,
                    JmapConnections(client, saved),
                    saved,
                    AccountService.Mail,
                    beforeRemove = { removing.cancel() },
                    onAccountChanged = { changed = true },
                )

            removing = launch { repository.remove(login.id) }
            removing.join()

            assertTrue(removing.isCancelled)
            assertEquals(listOf(login), saved.logins())
            assertEquals(false, changed)
        }
}
