package com.otus.jmap.sharing

import com.otus.jmap.ADA
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapIds
import com.otus.jmap.JmapJson
import com.otus.jmap.QueryMethod
import com.otus.jmap.anyOf
import com.otus.jmap.fixtureServer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrincipalTest {
    private val joe =
        Principal(
            id = "P2342fnddd20",
            type = PrincipalKind.Individual,
            name = "Joe Bloggs",
            email = "joe.bloggs@example.com",
            timeZone = "Australia/Melbourne",
            capabilities =
                mapOf(
                    "urn:com.example:jmap:todo" to
                        buildJsonObject {
                            put("accountId", JsonNull)
                            put("mayShareWith", JsonPrimitive(true))
                        },
                ),
        )

    @Test
    fun fetchesTheRfcExamplePrincipals() =
        runTest {
            val (engine, requests) = fixtureServer(SHARING_SESSION) { PRINCIPAL_GET_RESPONSE }
            val client = JmapClient(engine)
            val session = client.session(SESSION_URL, ADA)
            val result = client.request(session, ADA) { call(GetMethod(PrincipalType, "u33084183")) }
            assertEquals("7b8eff5zz", result.state)
            assertEquals(
                listOf(joe, Principal("P674pp24095qo49pr", PrincipalKind.Location, name = "Board room")),
                result.list,
            )
            val body = requests.last().body
            assertTrue("\"using\":[\"urn:ietf:params:jmap:core\",\"urn:ietf:params:jmap:principals\"]" in body, body)
            assertTrue("[\"Principal/get\",{\"accountId\":\"u33084183\",\"ids\":null},\"c0\"]" in body, body)
        }

    @Test
    fun aPartialGetLeavesOutTheTypeItDidNotAskFor() =
        runTest {
            // RFC 8620, section 5.1: with properties, the server returns only those and the id.
            val (engine, requests) =
                fixtureServer(SHARING_SESSION) {
                    """{"methodResponses": [["Principal/get", {"accountId": "u33084183", "state": "7b8eff5zz",
                       "list": [{"id": "P2342fnddd20", "name": "Joe Bloggs"}], "notFound": []}, "c0"]],
                       "sessionState": "s1"}"""
                }
            val client = JmapClient(engine)
            val get = GetMethod(PrincipalType, "u33084183", properties = listOf("name"))
            val result = client.request(client.session(SESSION_URL, ADA), ADA) { call(get) }
            assertEquals(listOf(Principal("P2342fnddd20", name = "Joe Bloggs")), result.list)
            assertNull(result.list.single().type)
            assertTrue("\"properties\":[\"name\"]" in requests.last().body, requests.last().body)
        }

    @Test
    fun decodesThePrincipalAccountsTheUserMayAccess() {
        val principal =
            decode(
                """{"id": "P1", "type": "individual", "name": "Jane Doe", "capabilities": {},
                   "accounts": {"u12345678": {"name": "jane.doe@example.com", "isPersonal": false,
                     "isReadOnly": true, "accountCapabilities": {"urn:ietf:params:jmap:contacts": {}}}}}""",
            )
        assertEquals(
            mapOf(
                "u12345678" to
                    PrincipalAccount(
                        name = "jane.doe@example.com",
                        isReadOnly = true,
                        accountCapabilities = mapOf(JmapCapability.CONTACTS to JsonObject(emptyMap())),
                    ),
            ),
            principal.accounts,
        )
    }

    @Test
    fun unknownKindsDecodeAndKeepTheirValue() {
        val principal = decode("""{"id": "P1", "type": "team", "name": "Design"}""")
        assertEquals(PrincipalKind("team"), principal.type)
        assertEquals(
            listOf("individual", "group", "resource", "location", "other"),
            listOf(
                PrincipalKind.Individual,
                PrincipalKind.Group,
                PrincipalKind.Resource,
                PrincipalKind.Location,
                PrincipalKind.Other,
            ).map { it.value },
        )
    }

    @Test
    fun principalsUseTheirOwnCapability() {
        assertEquals("Principal", PrincipalType.name)
        assertEquals(JmapCapability.PRINCIPALS, PrincipalType.capability)
        val get = GetMethod(PrincipalType, "u33084183", JmapIds.Of(listOf("P2342fnddd20")))
        assertEquals("Principal/get", get.name)
        assertEquals(JmapCapability.PRINCIPALS, get.capability)
    }

    @Test
    fun filterSendsOnlyTheGivenConditions() {
        val filter =
            PrincipalFilter(
                accountIds = listOf("u12345678", "u87654321"),
                email = "joe",
                name = "Bloggs",
                text = "Melbourne",
                type = PrincipalKind.Individual,
                timeZone = "Australia/Melbourne",
            )
        assertEquals(
            buildJsonObject {
                put("accountIds", JsonArray(listOf(JsonPrimitive("u12345678"), JsonPrimitive("u87654321"))))
                put("email", JsonPrimitive("joe"))
                put("name", JsonPrimitive("Bloggs"))
                put("text", JsonPrimitive("Melbourne"))
                put("type", JsonPrimitive("individual"))
                put("timeZone", JsonPrimitive("Australia/Melbourne"))
            },
            filter.toFilter().toJson(),
        )
        assertEquals(JsonObject(emptyMap()), PrincipalFilter().toFilter().toJson())
    }

    @Test
    fun filtersCombineIntoOneQuery() {
        val rooms = PrincipalFilter(type = PrincipalKind.Location).toFilter()
        val people = PrincipalFilter(type = PrincipalKind.Individual, text = "Joe").toFilter()
        val query = QueryMethod(PrincipalType, "u33084183", filter = anyOf(rooms, people).toJson())
        assertEquals(
            buildJsonObject {
                put("operator", JsonPrimitive("OR"))
                put(
                    "conditions",
                    JsonArray(
                        listOf(
                            buildJsonObject { put("type", JsonPrimitive("location")) },
                            buildJsonObject {
                                put("text", JsonPrimitive("Joe"))
                                put("type", JsonPrimitive("individual"))
                            },
                        ),
                    ),
                )
            },
            query.arguments().getValue("filter"),
        )
        assertEquals("Principal/query", query.name)
    }

    private fun decode(json: String): Principal = JmapJson.decodeFromString(PrincipalType.serializer, json)
}
