package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.domain.AccountStorageException
import com.otusfoss.core.accounts.domain.ConnectFailure
import com.otusfoss.core.accounts.domain.ConnectOutcome
import com.otusfoss.core.accounts.domain.JmapAccountDraft
import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.TestLifecycle
import com.otusfoss.core.accounts.testing.stalwartLike
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginLifecycleException
import com.otusfoss.core.jmap.login.LoginSecret
import com.otusfoss.core.jmap.login.LoginStore
import com.otusfoss.core.jmap.login.LoginStoreException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountStorageFailureTest {
    private val store = FailingLoginStore()
    private val client =
        JmapClient(
            stalwartLike {
                """{"methodResponses":[["Mailbox/get",{"list":[],"state":"s"},"c0"]]}"""
            },
        )
    private val connections = JmapConnections(client, store)
    private val repository = JmapAccountRepository(client, connections, store, MailService)

    @Test
    fun accountReadsAndRemovalsExposeDomainErrors() =
        runTest {
            store.failRead = true
            assertFailsWith<AccountStorageException> { repository.accounts() }
            assertFailsWith<AccountStorageException> { repository.remove("a") }
        }

    @Test
    fun aLifecycleThatCannotFinishStopsTheRemovalAsAStorageFailure() =
        runTest {
            val stopping =
                TestLifecycle(onRemoving = { throw LoginLifecycleException(IllegalStateException("cache locked")) })
            val removal = JmapAccountRepository(client, connections, store, MailService, listOf(stopping))
            assertFailsWith<AccountStorageException> { removal.remove("a") }
        }

    @Test
    fun failedCredentialSaveReportsStorageFailure() =
        runTest {
            val draft = JmapAccountDraft("https://mail.example.org/.well-known/jmap", "ada@example.org", "secret")
            assertEquals(ConnectOutcome.Failed(ConnectFailure.StorageFailed), repository.connect(draft))
        }
}

private class FailingLoginStore : LoginStore {
    var failRead = false

    override fun logins(): List<JmapLogin> {
        if (failRead) throw LoginStoreException("read failed")
        return emptyList()
    }

    override fun secret(id: String): LoginSecret = LoginSecret.Missing

    override fun save(
        login: JmapLogin,
        secret: String,
    ): Unit = throw LoginStoreException("save failed")

    override fun remove(id: String): Unit = throw LoginStoreException("remove failed")
}
