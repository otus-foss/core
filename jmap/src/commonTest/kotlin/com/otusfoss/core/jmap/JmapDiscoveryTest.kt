package com.otusfoss.core.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JmapDiscoveryTest {
    private val wellKnown = "https://mail.example.org/.well-known/jmap"

    @Test
    fun followsARelativeRedirectAndResolvesTheRelativeApiUrl() =
        runTest {
            val (engine, requests) = server()
            val session = JmapClient(engine).session(wellKnown, ADA)
            assertEquals("https://mail.example.org/jmap/session", session.sessionUrl)
            assertEquals("https://mail.example.org/jmap/", session.apiUrl)
            assertEquals("d1", session.accountFor(JmapCapability.MAIL))
            assertEquals("s1", session.state)
            assertTrue(requests.all { it.authorized }, "The origin the user entered receives credentials")
        }

    @Test
    fun capabilityObjectsAndTheUploadTemplateAreKept() =
        runTest {
            val engine =
                MockEngine {
                    json(
                        """{"capabilities": {"urn:ietf:params:jmap:core": {"maxObjectsInSet": 500,
                           "maxSizeUpload": "huge"}, "urn:example:odd": true},
                           "accounts": {"d1": {"name": "Ada", "accountCapabilities": {
                             "urn:ietf:params:jmap:sieve": {"sieveExtensions": ["fileinto", "mailboxid"]}}}},
                           "apiUrl": "/jmap/", "uploadUrl": "/upload/{accountId}/"}""",
                    )
                }
            val session = JmapClient(engine).session(wellKnown, ADA)
            assertEquals("/upload/{accountId}/", session.uploadUrl)
            assertEquals(500, session.maxObjectsInSet)
            assertEquals(null, session.maxSizeUpload, "A malformed limit reads as unknown")
            assertEquals(emptyMap(), session.capabilityObjects.getValue("urn:example:odd"))
            val sieve =
                session.accounts
                    .getValue("d1")
                    .capabilityObjects
                    .getValue(JmapCapability.SIEVE)
            assertTrue("mailboxid" in sieve.getValue("sieveExtensions").toString())
        }

    @Test
    fun credentialsNeverReachAnotherHostWithoutConsent() =
        runTest {
            val requests = mutableListOf<Recorded>()
            val engine =
                MockEngine { request ->
                    requests += request.record()
                    if (request.url.host == "mail.example.org") {
                        redirect("https://login.example.net/session")
                    } else {
                        json(session())
                    }
                }
            val error =
                assertFailsWith<JmapException.RedirectedElsewhere> { JmapClient(engine).session(wellKnown, ADA) }
            assertEquals("login.example.net", error.host)
            assertTrue(
                requests.none { it.host == "login.example.net" && it.authorized },
                "No credentials to an untrusted host",
            )

            requests.clear()
            val session = JmapClient(engine).session(wellKnown, ADA, setOf(error.origin))
            assertEquals("https://login.example.net/jmap/", session.apiUrl)
            assertTrue(requests.last().authorized, "An approved origin receives credentials")
        }

    @Test
    fun anApiUrlOnAnotherHostNeedsConsentToo() =
        runTest {
            val engine = MockEngine { json(session(apiUrl = "https://api.example.net/jmap/")) }
            val error =
                assertFailsWith<JmapException.RedirectedElsewhere> { JmapClient(engine).session(wellKnown, ADA) }
            assertEquals("api.example.net", error.host)
        }

    @Test
    fun anUnapprovedBlobHostFailsOnlyBlobCalls() =
        runTest {
            val (engine, requests) = blobHostServer()
            val client = JmapClient(engine)
            val session = client.session(wellKnown, ADA)
            assertEquals(
                listOf("a"),
                client.request(session, ADA) { call(GetMethod(MAILBOX, "d1")) }.list.map { it.id },
            )
            val error =
                assertFailsWith<JmapException.RedirectedElsewhere> {
                    client.download(session, ADA, "d1", "photo", "image/png", 10)
                }
            assertEquals(USERCONTENT, error.origin)
            assertTrue(requests.none { it.host == "usercontent.example" }, "Nothing goes to an unapproved host")
        }

    @Test
    fun signInCanAskForTheBlobHostBeforeCredentialsGoThere() =
        runTest {
            val (engine, requests) = blobHostServer()
            val client = JmapClient(engine)
            val error =
                assertFailsWith<JmapException.RedirectedElsewhere> {
                    client.session(wellKnown, ADA, requireTrustedBlobOrigins = true)
                }
            assertEquals(USERCONTENT, error.origin)
            assertTrue(requests.none { it.host == "usercontent.example" }, "Discovery sends nothing there")

            val session = client.session(wellKnown, ADA, setOf(error.origin), requireTrustedBlobOrigins = true)
            assertContentEquals(byteArrayOf(1), client.download(session, ADA, "d1", "photo", "image/png", 10).bytes)
            assertEquals("usercontent.example", requests.last().host)
            assertTrue(requests.last().authorized, "An approved blob origin receives credentials")
        }

    @Test
    fun signInChecksTheUploadHostButLeavesAnInsecureTemplateToFailWhenUsed() =
        runTest {
            val upload = MockEngine { json(withBlobs(""""uploadUrl": "https://upload.example/{accountId}/"""")) }
            val error =
                assertFailsWith<JmapException.RedirectedElsewhere> {
                    JmapClient(upload).session(wellKnown, ADA, requireTrustedBlobOrigins = true)
                }
            assertEquals("https://upload.example:443", error.origin)

            val insecure = MockEngine { json(withBlobs(""""downloadUrl": "http://mail.example.org/{blobId}"""")) }
            val client = JmapClient(insecure)
            val session = client.session(wellKnown, ADA, requireTrustedBlobOrigins = true)
            assertFailsWith<JmapException.InsecureUrl> { client.download(session, ADA, "d1", "photo", "image/png", 10) }
        }

    @Test
    fun plainHttpIsRefusedEverywhere() =
        runTest {
            val any = MockEngine { json(session()) }
            assertFailsWith<JmapException.InsecureUrl> {
                JmapClient(any).session("http://mail.example.org/jmap/session", ADA)
            }
            val downgrade = MockEngine { redirect("http://mail.example.org/jmap/session") }
            assertFailsWith<JmapException.InsecureUrl> { JmapClient(downgrade).session(wellKnown, ADA) }
            val insecureApi = MockEngine { json(session(apiUrl = "http://mail.example.org/jmap/")) }
            assertFailsWith<JmapException.InsecureUrl> { JmapClient(insecureApi).session(wellKnown, ADA) }
        }

    @Test
    fun httpIsAllowedOnlyByAnExplicitTestPolicy() =
        runTest {
            val engine = MockEngine { json(session()) }
            val session =
                JmapClient(engine, JmapPolicy(allowInsecureHttp = true))
                    .session("http://localhost:8080/jmap/session", ADA)
            assertEquals("http://localhost:8080/jmap/", session.apiUrl)
        }

    @Test
    fun statusCodesMapToDistinctFailures() =
        runTest {
            suspend fun failure(
                status: HttpStatusCode,
                body: String = "",
            ) = runCatching {
                JmapClient(MockEngine { respond(body, status) }).session(wellKnown, ADA)
            }.exceptionOrNull()
            assertTrue(failure(HttpStatusCode.Unauthorized) is JmapException.Unauthorized)
            assertTrue(failure(HttpStatusCode.Forbidden) is JmapException.Forbidden)
            assertTrue(failure(HttpStatusCode.NotFound) is JmapException.NotJmap)
            assertTrue(failure(HttpStatusCode.OK, "<html></html>") is JmapException.NotJmap)
            val limit = """{"type": "urn:ietf:params:jmap:error:limit", "limit": "maxSizeRequest"}"""
            val problem = assertIs<JmapException.RequestFailed>(failure(HttpStatusCode.BadRequest, limit))
            assertEquals("urn:ietf:params:jmap:error:limit", problem.type)
            assertEquals("maxSizeRequest", problem.limit)
            val plain = assertIs<JmapException.RequestFailed>(failure(HttpStatusCode.InternalServerError))
            assertEquals(null, plain.limit)
        }

    @Test
    fun redirectLoopsAndBadUrlsFailCleanly() =
        runTest {
            val loop = MockEngine { redirect("/.well-known/jmap") }
            assertFailsWith<JmapException.TooManyRedirects> { JmapClient(loop).session(wellKnown, ADA) }
            assertFailsWith<JmapException.InsecureUrl> { JmapClient(loop).session("not a url", ADA) }
        }
}

/** The fixture session with the given blob template properties added. */
private fun withBlobs(templates: String): String = session().dropLast(1) + ", $templates}"

private const val USERCONTENT = "https://usercontent.example:443"

/** A server whose downloads live on `usercontent.example`; it answers Mailbox/get and blob requests. */
private fun blobHostServer(): Pair<MockEngine, MutableList<Recorded>> {
    val requests = mutableListOf<Recorded>()
    val mailboxes = """{"methodResponses": [["Mailbox/get", {"state": "m", "list": [{"id": "a"}]}, "c0"]]}"""
    val session = withBlobs(""""downloadUrl": "https://usercontent.example/{accountId}/{blobId}"""")
    val png = headersOf(HttpHeaders.ContentType, "image/png")
    val engine =
        MockEngine { request ->
            val record = request.record()
            requests += record
            when {
                record.host == "usercontent.example" -> respond(byteArrayOf(1), headers = png)
                record.path == "/jmap/" -> json(mailboxes)
                else -> json(session)
            }
        }
    return engine to requests
}
