package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.TestLifecycle
import com.otusfoss.core.accounts.testing.stalwartLike
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.JmapException
import com.otusfoss.core.jmap.login.InMemoryLoginStore
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginStore
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
                    MailService,
                    listOf(TestLifecycle(onRemoved = { changed += it })),
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
                    MailService,
                    listOf(
                        TestLifecycle(
                            onRemoved = {
                                removing.cancel()
                                yield()
                                cleaned = true
                            },
                        ),
                    ),
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
            val failed = mutableListOf<String>()
            val repository =
                JmapAccountRepository(
                    client,
                    JmapConnections(client, saved),
                    saved,
                    MailService,
                    listOf(
                        TestLifecycle(
                            onRemoving = { removing.cancel() },
                            onRemoveFailed = { failed += it },
                            onRemoved = { changed = true },
                        ),
                    ),
                )

            removing = launch { repository.remove(login.id) }
            removing.join()

            assertTrue(removing.isCancelled)
            assertEquals(listOf(login), saved.logins())
            assertEquals(false, changed)
            assertEquals(listOf(login.id), failed)
        }

    @Test
    fun aFailingRemovalStepKeepsTheLoginAndReportsTheStop() =
        runTest {
            val failed = mutableListOf<String>()
            val repository =
                JmapAccountRepository(
                    client,
                    JmapConnections(client, saved),
                    saved,
                    MailService,
                    listOf(
                        TestLifecycle(onRemoveFailed = { failed += it }),
                        TestLifecycle(onRemoving = { error("cleanup failed") }),
                    ),
                )

            assertFailsWith<IllegalStateException> { repository.remove(login.id) }

            assertEquals(listOf(login), saved.logins())
            assertEquals(listOf(login.id), failed)
        }
}
