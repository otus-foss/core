package com.otus.jmap.sharing

import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapException
import com.otus.jmap.JmapSort
import com.otus.jmap.QueryMethod
import com.otus.jmap.SetMethod
import com.otus.jmap.jmapDecoding
import com.otus.jmap.parseObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class ShareNotificationTest {
    @Test
    fun decodesNotificationsWithTheRfcProperties() {
        // RFC 9670 has no ShareNotification example; these follow the property list in section 3.
        val result =
            get(
                """{"accountId": "u33084183", "state": "n1", "notFound": [], "list": [
                   {"id": "sn1", "created": "2024-11-05T09:30:00Z",
                    "changedBy": {"name": "Joe Bloggs", "email": "joe.bloggs@example.com",
                      "principalId": "P2342fnddd20"},
                    "objectType": "AddressBook", "objectAccountId": "u87654321", "objectId": "ab1",
                    "oldRights": null,
                    "newRights": {"mayRead": true, "mayWrite": false, "mayShare": false, "mayDelete": false},
                    "name": "Team contacts"},
                   {"id": "sn2", "created": "2024-11-06T10:00:00Z",
                    "changedBy": {"name": "Admin", "email": null, "principalId": null},
                    "objectType": "AddressBook", "objectAccountId": "u87654321", "objectId": "ab1",
                    "oldRights": {"mayRead": true, "mayWrite": false, "mayShare": false, "mayDelete": false},
                    "newRights": null, "name": "Team contacts"}]}""",
            )
        val rights = mapOf("mayRead" to true, "mayWrite" to false, "mayShare" to false, "mayDelete" to false)
        assertEquals(
            listOf(
                ShareNotification(
                    id = "sn1",
                    created = Instant.parse("2024-11-05T09:30:00Z"),
                    changedBy = ShareEntity("Joe Bloggs", "joe.bloggs@example.com", "P2342fnddd20"),
                    objectType = "AddressBook",
                    objectAccountId = "u87654321",
                    objectId = "ab1",
                    newRights = rights,
                    name = "Team contacts",
                ),
                ShareNotification(
                    id = "sn2",
                    created = Instant.parse("2024-11-06T10:00:00Z"),
                    changedBy = ShareEntity("Admin"),
                    objectType = "AddressBook",
                    objectAccountId = "u87654321",
                    objectId = "ab1",
                    oldRights = rights,
                    name = "Team contacts",
                ),
            ),
            result.list,
        )
    }

    @Test
    fun aPartialGetLeavesOutWhatItDidNotAskFor() {
        // RFC 8620, section 5.1: with properties ["created", "name"], the server returns only those and the id.
        val result =
            get(
                """{"accountId": "u33084183", "state": "n1", "notFound": [],
                   "list": [{"id": "sn1", "created": "2024-11-05T09:30:00Z", "name": "Team contacts"}]}""",
            )
        assertEquals(
            listOf(ShareNotification("sn1", created = Instant.parse("2024-11-05T09:30:00Z"), name = "Team contacts")),
            result.list,
        )
    }

    @Test
    fun anUnreadableCreationDateIsNotJmap() {
        assertFailsWith<JmapException.NotJmap> {
            get(
                """{"state": "n1", "list": [{"id": "sn1", "created": "yesterday", "changedBy": {"name": "Joe"},
                   "objectType": "AddressBook", "objectAccountId": "u1", "objectId": "ab1"}]}""",
            )
        }
    }

    @Test
    fun filterSendsUtcDatesAndExactMatches() {
        val filter =
            ShareNotificationFilter(
                after = Instant.parse("2024-11-01T00:00:00Z"),
                before = Instant.parse("2024-12-01T00:00:00Z"),
                objectType = "AddressBook",
                objectAccountId = "u87654321",
            )
        assertEquals(
            buildJsonObject {
                put("after", JsonPrimitive("2024-11-01T00:00:00Z"))
                put("before", JsonPrimitive("2024-12-01T00:00:00Z"))
                put("objectType", JsonPrimitive("AddressBook"))
                put("objectAccountId", JsonPrimitive("u87654321"))
            },
            filter.toFilter().toJson(),
        )
        assertEquals(JsonObject(emptyMap()), ShareNotificationFilter().toFilter().toJson())
    }

    @Test
    fun queriesSortByCreationTime() {
        assertEquals(JmapSort("created"), ShareNotificationSort.created())
        val query =
            QueryMethod(
                ShareNotificationType,
                "u33084183",
                filter = ShareNotificationFilter(objectType = "AddressBook").toFilter().toJson(),
                sort = listOf(ShareNotificationSort.created(isAscending = false)),
            ).arguments()
        assertEquals(
            JsonArray(
                listOf(
                    buildJsonObject {
                        put("property", JsonPrimitive("created"))
                        put("isAscending", JsonPrimitive(false))
                    },
                ),
            ),
            query.getValue("sort"),
        )
        assertEquals(buildJsonObject { put("objectType", JsonPrimitive("AddressBook")) }, query.getValue("filter"))
    }

    @Test
    fun dismissingDestroysTheNotification() {
        val dismiss = SetMethod(ShareNotificationType, "u33084183", destroy = listOf("sn1"))
        assertEquals("ShareNotification/set", dismiss.name)
        assertEquals(JmapCapability.PRINCIPALS, dismiss.capability)
        assertEquals(JsonArray(listOf(JsonPrimitive("sn1"))), dismiss.arguments().getValue("destroy"))
    }

    private fun get(body: String): GetResult<ShareNotification> =
        jmapDecoding { GetMethod(ShareNotificationType, "u33084183").decode(parseObject(body)) }
}
