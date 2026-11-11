package com.otusfoss.core.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JmapServerProbeTest {
    private val wellKnown = "https://mail.example.org/.well-known/jmap"

    @Test
    fun aServerThatAsksForCredentialsAfterARedirectIsJmap() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertFalse(HttpHeaders.Authorization in request.headers, "The probe sends no credentials")
                    if (request.url.encodedPath == "/.well-known/jmap") {
                        redirect("/jmap/session")
                    } else {
                        respond("", HttpStatusCode.Unauthorized)
                    }
                }
            assertTrue(JmapClient(engine).isJmapServer(wellKnown))
        }

    @Test
    fun aPublicSessionWithTheCoreCapabilityIsJmap() =
        runTest {
            assertTrue(JmapClient(MockEngine { json(session()) }).isJmapServer(wellKnown))
        }

    @Test
    fun aMissingPageOrAnyOtherJsonIsNotJmap() =
        runTest {
            assertFalse(JmapClient(MockEngine { respond("", HttpStatusCode.NotFound) }).isJmapServer(wellKnown))
            assertFalse(JmapClient(MockEngine { json("""{"capabilities": {}}""") }).isJmapServer(wellKnown))
            assertFalse(JmapClient(MockEngine { respond("<html></html>") }).isJmapServer(wellKnown))
        }
}
