package com.otus.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JmapUrlResolutionTest {
    private val urls = JmapUrls(JmapPolicy())

    @Test
    fun resolvesTheRfc3986ReferenceExamplesWithoutCarryingOldComponents() {
        val base = "https://a/b/c/d;p?q"
        val examples =
            mapOf(
                "g" to "https://a/b/c/g",
                "./g" to "https://a/b/c/g",
                "g/" to "https://a/b/c/g/",
                "/g" to "https://a/g",
                "//g" to "https://g",
                "?y" to "https://a/b/c/d;p?y",
                "g?y" to "https://a/b/c/g?y",
                "#s" to "https://a/b/c/d;p?q#s",
                "g#s" to "https://a/b/c/g#s",
                "g?y#s" to "https://a/b/c/g?y#s",
                ";x" to "https://a/b/c/;x",
                "g;x" to "https://a/b/c/g;x",
                "g;x?y#s" to "https://a/b/c/g;x?y#s",
                "" to base,
                "." to "https://a/b/c/",
                "./" to "https://a/b/c/",
                ".." to "https://a/b/",
                "../" to "https://a/b/",
                "../g" to "https://a/b/g",
                "../.." to "https://a/",
                "../../" to "https://a/",
                "../../g" to "https://a/g",
                "../../../g" to "https://a/g",
                "/./g" to "https://a/g",
                "/../g" to "https://a/g",
                "g/./h" to "https://a/b/c/g/h",
                "g/../h" to "https://a/b/c/h",
                "g?y/../x" to "https://a/b/c/g?y/../x",
                "g#s/../x" to "https://a/b/c/g#s/../x",
                "https://other/api" to "https://other/api",
            )
        examples.forEach { (reference, expected) ->
            assertEquals(expected, urls.resolve(urls.parse(base), reference).toString(), reference)
        }
    }

    @Test
    fun preservesEmptyQueriesEscapesAndRepeatedSlashes() {
        val base = urls.parse("https://user:password@example.org/session?route=old#old")
        val examples =
            mapOf(
                "?route=api" to "https://user:password@example.org/session?route=api",
                "?" to "https://user:password@example.org/session?",
                "/api" to "https://user:password@example.org/api",
                "/a//b/../c/%2E%2E/d?x=a%2Fb" to "https://user:password@example.org/a//c/%2E%2E/d?x=a%2Fb",
                "https://other/api" to "https://other/api",
            )
        examples.forEach { (reference, expected) ->
            assertEquals(expected, urls.resolve(base, reference).toString(), reference)
        }
    }

    @Test
    fun unapprovedRedirectReceivesNeitherAuthorizationNorTheOriginalQuery() =
        runTest {
            var redirectedUrl = ""
            val client =
                JmapClient(
                    MockEngine { request ->
                        if (request.url.host == "example.org") {
                            redirect("https://other.example/session")
                        } else {
                            assertNull(request.headers[HttpHeaders.Authorization])
                            redirectedUrl = request.url.toString()
                            json(session())
                        }
                    },
                )
            assertFailsWith<JmapException.RedirectedElsewhere> {
                client.session("https://example.org/session?ticket=private", ADA)
            }
            assertEquals("https://other.example/session", redirectedUrl)
        }

    @Test
    fun sessionEndpointsDoNotInheritDiscoveryParameters() =
        runTest {
            val client = JmapClient(MockEngine { json(session(apiUrl = "/api?route=api")) })
            val session = client.session("https://example.org/session?route=session", ADA)
            assertEquals("https://example.org/api?route=api", session.apiUrl)
            assertEquals(
                "https://example.org/download/blob?type=image%2Fpng",
                urls.trustedTarget(session, "/download/blob?type=image%2Fpng").toString(),
            )
        }
}
