package com.otusfoss.core.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JmapAccountCapabilityTest {
    @Serializable
    private data class Sieve(
        val maxSizeScript: Long? = null,
    )

    @Test
    fun anAccountCapabilityReadsAsItsType() {
        assertEquals(Sieve(4096), session(JsonPrimitive(4096)).sieve("a1", JmapCapability.SIEVE))
    }

    @Test
    fun aMissingOrMalformedCapabilityReadsAsNone() {
        assertNull(session(JsonPrimitive("large")).sieve("a1", JmapCapability.SIEVE))
        assertNull(session(JsonPrimitive(1)).sieve("a2", JmapCapability.SIEVE))
        assertNull(session(JsonPrimitive(1)).sieve("a1", JmapCapability.MAIL))
    }

    private fun JmapSession.sieve(
        accountId: String,
        capability: String,
    ): Sieve? = accountCapability(accountId, capability, Sieve.serializer())

    private fun session(maxSizeScript: JsonPrimitive): JmapSession =
        JmapSession(
            sessionUrl = "https://mail.example.org/jmap/session",
            apiUrl = "https://mail.example.org/jmap/",
            username = "ada@example.org",
            capabilities = setOf(JmapCapability.CORE, JmapCapability.SIEVE),
            accounts =
                mapOf(
                    "a1" to
                        JmapAccount(
                            name = "ada@example.org",
                            isPersonal = true,
                            isReadOnly = false,
                            capabilities = setOf(JmapCapability.SIEVE),
                            capabilityObjects =
                                mapOf(JmapCapability.SIEVE to JsonObject(mapOf("maxSizeScript" to maxSizeScript))),
                        ),
                ),
            primaryAccounts = emptyMap(),
            state = "s1",
            trustedOrigins = setOf("https://mail.example.org:443"),
        )
}
