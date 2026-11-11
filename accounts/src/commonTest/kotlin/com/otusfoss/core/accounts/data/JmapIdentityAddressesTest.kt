package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectOutcome
import com.otusfoss.core.accounts.domain.JmapAccountDraft
import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.STALWART_SESSION
import com.otusfoss.core.accounts.testing.stalwartLike
import com.otusfoss.core.jmap.JmapCapability
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.login.InMemoryLoginStore
import com.otusfoss.core.jmap.login.JmapLogin
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JmapIdentityAddressesTest {
    private val store = InMemoryLoginStore()
    private val login = JmapLogin("ada", "https://mail.example.org/.well-known/jmap", "ada@example.org", "Ada")
    private val session =
        STALWART_SESSION.replace(
            "\"urn:ietf:params:jmap:mail\": {}",
            "\"urn:ietf:params:jmap:mail\": {}, \"urn:ietf:params:jmap:submission\": {}",
        )
    private val identities = """{"methodResponses": [["Identity/get", {"state": "i", "list": [
        {"email": "ada@example.org"}, {"email": " work@other.example "}, {"email": "Ada@example.org"},
        {"email": "*@example.org"}, {"email": ""}]}, "c0"]]}"""

    private fun repository(
        engine: MockEngine,
        service: AccountService = MailService,
    ): JmapAccountRepository {
        store.save(login, "secret")
        val client = JmapClient(engine)
        return JmapAccountRepository(client, JmapConnections(client, store), store, service)
    }

    @Test
    fun savedAccountsDiscoverDistinctSendingAddressesFromTheirMailAccount() =
        runTest {
            val repository =
                repository(
                    stalwartLike(session) { body ->
                        val request = Json.parseToJsonElement(body).jsonObject
                        assertEquals(
                            listOf(JmapCapability.CORE, JmapCapability.SUBMISSION),
                            request.getValue("using").jsonArray.map { it.jsonPrimitive.content },
                        )
                        val call =
                            request
                                .getValue("methodCalls")
                                .jsonArray
                                .single()
                                .jsonArray
                        assertEquals("Identity/get", call[0].jsonPrimitive.content)
                        val arguments = call[1].jsonObject
                        assertEquals("d333333", arguments.getValue("accountId").jsonPrimitive.content)
                        assertEquals("null", arguments.getValue("ids").toString())
                        assertEquals(
                            listOf("email"),
                            arguments.getValue("properties").jsonArray.map { it.jsonPrimitive.content },
                        )
                        identities
                    },
                )
            assertEquals(
                listOf("ada@example.org", "work@other.example", "*@example.org"),
                repository.accounts().single().emailAddresses,
            )
        }

    @Test
    fun mailWithoutSubmissionDoesNotRequestIdentities() =
        runTest {
            val repository = repository(stalwartLike())
            assertEquals(emptyList(), repository.accounts().single().emailAddresses)
        }

    @Test
    fun submissionOnAnotherAccountDoesNotSupplyThisAccountsAddresses() =
        runTest {
            val otherAccount =
                STALWART_SESSION
                    .replace(
                        "\"urn:ietf:params:jmap:core\": {}",
                        "\"urn:ietf:params:jmap:core\": {}, \"urn:ietf:params:jmap:submission\": {}",
                    ).replace(
                        "\"accounts\": {",
                        "\"accounts\": {\"other\": {\"accountCapabilities\": " +
                            "{\"urn:ietf:params:jmap:submission\": {}}},",
                    )
            val repository = repository(stalwartLike(otherAccount))
            assertEquals(emptyList(), repository.accounts().single().emailAddresses)
        }

    @Test
    fun aServiceWithoutItsAccountDiscoversNoIdentities() =
        runTest {
            val repository = repository(stalwartLike(session), AccountService("tasks", "urn:example:tasks", "TaskList"))
            assertEquals(emptyList(), repository.accounts().single().emailAddresses)
        }

    @Test
    fun anIdentityErrorKeepsTheSavedAccountAndDoesNotBlockConnecting() =
        runTest {
            val repository =
                repository(
                    stalwartLike(session) { body ->
                        if ("Identity/get" in body) {
                            """{"methodResponses": [["error", {"type": "forbidden"}, "c0"]]}"""
                        } else {
                            """{"methodResponses": [["Mailbox/get", {"state": "m", "list": []}, "c0"]]}"""
                        }
                    },
                )
            val draft = JmapAccountDraft(login.sessionUrl, login.username, "secret")
            assertIs<ConnectOutcome.Connected>(repository.connect(draft))
            val account = repository.accounts().single()
            assertEquals(login.id, account.id)
            assertEquals(emptyList(), account.emailAddresses)
        }

    @Test
    fun rejectedCredentialsKeepTheSavedAccountVisible() =
        runTest {
            val repository = repository(stalwartLike(session))
            store.save(login, "wrong")
            assertEquals(login.id, repository.accounts().single().id)
        }

    @Test
    fun addressDiscoveryDoesNotSwallowCancellation() =
        runTest {
            val repository = repository(stalwartLike(session) { throw CancellationException("cancel discovery") })
            assertFailsWith<CancellationException> { repository.accounts() }
        }
}
