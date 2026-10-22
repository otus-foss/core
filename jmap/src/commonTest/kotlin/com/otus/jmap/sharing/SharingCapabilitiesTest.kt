package com.otus.jmap.sharing

import com.otus.jmap.ADA
import com.otus.jmap.JmapAccount
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapJson
import com.otus.jmap.capabilityObjects
import com.otus.jmap.fixtureServer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SharingCapabilitiesTest {
    @Test
    fun readsTheOwnerOfTheRfcExampleAccount() =
        runTest {
            val (engine, _) = fixtureServer(SHARING_SESSION) { error("nothing reaches the API") }
            val session = JmapClient(engine).session(SESSION_URL, ADA)
            val jane = session.accounts.getValue("u12345678")
            assertEquals(PrincipalOwner("u33084183", "P105aga511jaa"), jane.principalOwner)
            assertNull(jane.principals)
        }

    @Test
    fun findsThePrincipalsAccountAndTheCurrentUser() =
        runTest {
            val (engine, _) = fixtureServer(SHARING_SESSION) { error("nothing reaches the API") }
            val session = JmapClient(engine).session(SESSION_URL, ADA)
            val directory = session.accountFor(JmapCapability.PRINCIPALS)
            assertEquals("u33084183", directory)
            val principals = session.accounts.getValue("u33084183")
            assertEquals(PrincipalsCapability("P105aga511jaa"), principals.principals)
            assertNull(principals.principalOwner)
            assertFalse(JmapCapability.PRINCIPALS_OWNER in session.capabilities)
        }

    @Test
    fun aNullCurrentUserPrincipalStillMeansPrincipalsAreSupported() {
        val account = account("""{"urn:ietf:params:jmap:principals": {"currentUserPrincipalId": null}}""")
        assertEquals(PrincipalsCapability(currentUserPrincipalId = null), account.principals)
    }

    @Test
    fun malformedCapabilitiesReadAsUnknown() {
        listOf(
            """{"urn:ietf:params:jmap:principals:owner": {"accountIdForPrincipal": "u1"}}""",
            """{"urn:ietf:params:jmap:principals:owner": "u1"}""",
            """{"urn:ietf:params:jmap:principals:owner": {"accountIdForPrincipal": {}, "principalId": "P1"}}""",
        ).forEach { assertNull(account(it).principalOwner, it) }
        assertNull(account("""{"urn:ietf:params:jmap:principals": {"currentUserPrincipalId": []}}""").principals)
    }

    @Test
    fun accountsWithoutSharingHaveNeitherCapability() {
        val account = account("""{"urn:ietf:params:jmap:contacts": {}}""")
        assertNull(account.principals)
        assertNull(account.principalOwner)
    }

    @Test
    fun ownerReadsExtraPropertiesLeniently() {
        val account =
            account(
                """{"urn:ietf:params:jmap:principals:owner":
                   {"accountIdForPrincipal": "u1", "principalId": "P1", "vendorHint": true}}""",
            )
        assertEquals(PrincipalOwner("u1", "P1"), account.principalOwner)
    }

    private fun account(capabilities: String): JmapAccount {
        val objects = JmapJson.parseToJsonElement(capabilities).jsonObject.capabilityObjects()
        return JmapAccount(
            name = "Jane",
            isPersonal = true,
            isReadOnly = false,
            capabilities = objects.keys,
            capabilityObjects = objects,
        )
    }
}
