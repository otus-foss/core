package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectFailure
import com.otusfoss.core.accounts.domain.ConnectOutcome
import com.otusfoss.core.accounts.domain.JmapAccountDraft
import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.STALWART_SESSION
import com.otusfoss.core.accounts.testing.TestLifecycle
import com.otusfoss.core.accounts.testing.stalwartLike
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.login.InMemoryLoginStore
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginSecret
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JmapAccountRepositoryTest {
    private val draft = JmapAccountDraft("https://mail.example.org/.well-known/jmap", "ada@example.org", "secret")
    private val mailboxes = """{"methodResponses": [["Mailbox/get", {"state": "m", "list": [{"id": "a"}]}, "c0"]]}"""
    private val store = InMemoryLoginStore()

    private fun repository(engine: MockEngine): JmapAccountRepository {
        val client = JmapClient(engine)
        return JmapAccountRepository(client, JmapConnections(client, store), store, MailService)
    }

    @Test
    fun connectingChecksTheServiceChosenByTheApp() =
        runTest {
            val capability = "urn:example:tasks"
            val session = STALWART_SESSION.replace("urn:ietf:params:jmap:mail", capability)
            val engine =
                stalwartLike(session) { body ->
                    val request = Json.parseToJsonElement(body).jsonObject
                    assertTrue(request.getValue("using").jsonArray.any { it.jsonPrimitive.content == capability })
                    val call =
                        request
                            .getValue("methodCalls")
                            .jsonArray
                            .single()
                            .jsonArray
                    assertEquals("TaskList/get", call[0].jsonPrimitive.content)
                    assertEquals(
                        "d333333",
                        call[1]
                            .jsonObject
                            .getValue("accountId")
                            .jsonPrimitive.content,
                    )
                    """{"methodResponses": [["TaskList/get", {"state": "t", "list": [{"id": "a"}]}, "c0"]]}"""
                }
            val client = JmapClient(engine)
            val repository =
                JmapAccountRepository(
                    client,
                    JmapConnections(client, store),
                    store,
                    AccountService("tasks", capability, "TaskList"),
                )
            assertIs<ConnectOutcome.Connected>(repository.connect(draft))
            assertEquals(1, store.logins().size)
        }

    @Test
    fun connectingVerifiesMailAndSavesTheLoginWithItsSecret() =
        runTest {
            val connected = assertIs<ConnectOutcome.Connected>(repository(stalwartLike { mailboxes }).connect(draft))
            assertEquals("ada@example.org", connected.account.name)
            assertEquals("mail.example.org", connected.account.server)
            val saved = store.logins().single()
            assertEquals(draft.sessionUrl, saved.sessionUrl)
            assertIs<LoginSecret.Found>(store.secret(saved.id))
        }

    @Test
    fun reconnectingReplacesTheExistingLogin() =
        runTest {
            store.save(JmapLogin("earlier-id", draft.sessionUrl, draft.username, "Ada"), "old")
            repository(stalwartLike { mailboxes }).connect(draft)
            assertEquals(listOf("earlier-id"), store.logins().map { it.id })
        }

    @Test
    fun removingRunsTheHookWhileTheCredentialsStillExist() =
        runTest {
            store.save(JmapLogin("ada", draft.sessionUrl, draft.username, "Ada"), "secret")
            val client = JmapClient(stalwartLike { mailboxes })
            var secretAtHook: LoginSecret? = null
            JmapAccountRepository(
                client,
                JmapConnections(client, store),
                store,
                MailService,
                listOf(TestLifecycle(onRemoving = { secretAtHook = store.secret(it) })),
            ).remove("ada")
            assertIs<LoginSecret.Found>(secretAtHook)
            assertTrue(store.logins().isEmpty())
        }

    @Test
    fun failuresSaveNothing() =
        runTest {
            val wrong = repository(stalwartLike()).connect(draft.copy(password = "other"))
            assertEquals(ConnectOutcome.Failed(ConnectFailure.WrongCredentials), wrong)
            val noMail = STALWART_SESSION.replace("urn:ietf:params:jmap:mail", "urn:ietf:params:jmap:calendars")
            assertEquals(
                ConnectOutcome.Failed(ConnectFailure.MissingService),
                repository(stalwartLike(noMail)).connect(draft),
            )
            val colon = repository(stalwartLike()).connect(draft.copy(username = "a:da"))
            assertEquals(ConnectOutcome.Failed(ConnectFailure.InvalidUsername), colon)
            assertTrue(store.logins().isEmpty())
        }

    @Test
    fun anotherHostNeedsConsentAndIsRememberedOnceTrusted() =
        runTest {
            val engine =
                MockEngine { request ->
                    if (request.url.host == "mail.example.org") {
                        respond(
                            "",
                            HttpStatusCode.TemporaryRedirect,
                            headersOf(HttpHeaders.Location, "https://login.example.net/s"),
                        )
                    } else {
                        val body = if (request.url.encodedPath == "/s") STALWART_SESSION else mailboxes
                        respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                    }
                }
            val confirm = assertIs<ConnectOutcome.ConfirmHost>(repository(engine).connect(draft))
            assertEquals("login.example.net", confirm.host)
            assertTrue(store.logins().isEmpty())
            assertIs<ConnectOutcome.Connected>(
                repository(engine).connect(draft.copy(trustedOrigins = setOf(confirm.origin))),
            )
            assertEquals(setOf(confirm.origin), store.logins().single().trustedOrigins)
        }

    @Test
    fun aBlobHostOnAnotherOriginIsApprovedBeforeSavingAndThenServesDownloads() =
        runTest {
            val session =
                STALWART_SESSION.trim().dropLast(1) + """, "downloadUrl": "https://files.example.net/{blobId}"}"""
            val engine =
                MockEngine { request ->
                    val json = headersOf(HttpHeaders.ContentType, "application/json")
                    when {
                        request.url.host == "files.example.net" -> respond(byteArrayOf(7), HttpStatusCode.OK)
                        request.url.encodedPath == "/jmap/" -> respond(mailboxes, HttpStatusCode.OK, json)
                        else -> respond(session, HttpStatusCode.OK, json)
                    }
                }
            val confirm = assertIs<ConnectOutcome.ConfirmHost>(repository(engine).connect(draft))
            assertEquals("files.example.net", confirm.host)
            assertTrue(store.logins().isEmpty())

            assertIs<ConnectOutcome.Connected>(
                repository(engine).connect(draft.copy(trustedOrigins = setOf(confirm.origin))),
            )
            val login = store.logins().single()
            assertEquals(setOf(confirm.origin), login.trustedOrigins)
            val connection = JmapConnections(JmapClient(engine), store).connection(login)
            assertContentEquals(byteArrayOf(7), connection.download("d333333", "photo", "image/png", 10).bytes)
        }

    @Test
    fun checkingAServerNeedsNoCredentialsAndSavesNothing() =
        runTest {
            assertEquals(null, repository(stalwartLike()).checkServer(draft.sessionUrl))
            val missing = MockEngine { respond("", HttpStatusCode.NotFound) }
            assertEquals(ConnectFailure.NotJmap, repository(missing).checkServer(draft.sessionUrl))
            assertTrue(store.logins().isEmpty())
        }
}
