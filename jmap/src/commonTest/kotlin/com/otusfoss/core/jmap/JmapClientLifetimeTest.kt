package com.otusfoss.core.jmap

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class JmapClientLifetimeTest {
    @Test
    fun closingTwiceRejectsNewRequestsAndLeavesAnInjectedEngineUsable() =
        runTest {
            var requests = 0
            val engine =
                MockEngine {
                    requests += 1
                    respond(session())
                }
            val client = JmapClient(engine)
            try {
                client.close()
                client.close()
                assertFails { client.session("https://mail.example.org/jmap", JmapAuth.Basic("a", "b")) }
                assertEquals(0, requests)
                assertTrue(engine.coroutineContext[Job]?.isActive == true)
                HttpClient(engine).use { other ->
                    assertEquals(200, other.get("https://mail.example.org/health").status.value)
                }
            } finally {
                client.close()
                engine.close()
            }
        }
}
