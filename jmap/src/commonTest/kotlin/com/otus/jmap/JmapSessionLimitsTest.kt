package com.otus.jmap

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JmapSessionLimitsTest {
    @Test
    fun theGetLimitComesFromTheCoreCapability() {
        // The core capability example of RFC 8620, section 2.1.
        assertEquals(256, session(JsonPrimitive(256)).maxObjectsInGet)
        assertNull(session(null).maxObjectsInGet)
    }

    @Test
    fun aMalformedGetLimitReadsAsUnknown() {
        listOf(JsonPrimitive(-1), JsonPrimitive("256"), JsonPrimitive(true)).forEach { value ->
            assertNull(session(value).maxObjectsInGet, value.toString())
        }
    }

    private fun session(maxObjectsInGet: JsonPrimitive?): JmapSession =
        JmapSession(
            sessionUrl = "https://mail.example.org/jmap/session",
            apiUrl = "https://mail.example.org/jmap/",
            username = "ada@example.org",
            capabilities = setOf(JmapCapability.CORE),
            accounts = emptyMap(),
            primaryAccounts = emptyMap(),
            state = "s1",
            trustedOrigins = setOf("https://mail.example.org:443"),
            capabilityObjects =
                mapOf(
                    JmapCapability.CORE to
                        buildJsonObject { if (maxObjectsInGet != null) put("maxObjectsInGet", maxObjectsInGet) },
                ),
        )
}
