package com.otusfoss.core.jmap.contacts

import com.otusfoss.core.jmap.JmapSort
import com.otusfoss.core.jmap.QueryMethod
import com.otusfoss.core.jmap.allOf
import com.otusfoss.core.jmap.noneOf
import com.otusfoss.core.jscontact.CardKind
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ContactCardFilterTest {
    @Test
    fun everyConditionIsSentUnderItsRfcName() {
        // RFC 9610, section 3.3.1, in the order the RFC lists the conditions.
        val filter =
            ContactCardFilter(
                inAddressBook = PERSONAL,
                uid = "22B2C7DF-9120-4969-8460-05956FE6B065",
                hasMember = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af",
                kind = CardKind.Group,
                createdBefore = Instant.parse("2024-12-01T00:00:00Z"),
                createdAfter = Instant.parse("2024-01-01T00:00:00Z"),
                updatedBefore = Instant.parse("2025-06-30T12:00:00Z"),
                updatedAfter = Instant.parse("2025-01-01T08:30:00Z"),
                text = "bloggs",
                name = "Joe",
                nameGiven = "Joe",
                nameSurname = "Bloggs",
                nameSurname2 = "Smith",
                nickname = "Joey",
                organization = "ABC, Inc.",
                email = "joe.bloggs@example.com",
                phone = "+1-555-555-5555",
                onlineService = "xmpp:joe@example.com",
                address = "Main Street",
                note = "\"met at the conference\"",
            )
        assertEquals(
            parseObject(
                """{"inAddressBook": "$PERSONAL", "uid": "22B2C7DF-9120-4969-8460-05956FE6B065",
                "hasMember": "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af", "kind": "group",
                "createdBefore": "2024-12-01T00:00:00Z", "createdAfter": "2024-01-01T00:00:00Z",
                "updatedBefore": "2025-06-30T12:00:00Z", "updatedAfter": "2025-01-01T08:30:00Z",
                "text": "bloggs", "name": "Joe", "name/given": "Joe", "name/surname": "Bloggs",
                "name/surname2": "Smith", "nickname": "Joey", "organization": "ABC, Inc.",
                "email": "joe.bloggs@example.com", "phone": "+1-555-555-5555",
                "onlineService": "xmpp:joe@example.com", "address": "Main Street",
                "note": "\"met at the conference\""}""",
            ),
            filter.toFilter().toJson(),
        )
    }

    @Test
    fun anEmptyConditionMatchesEveryCard() {
        // RFC 9610, section 3.3.1: a condition with no properties always evaluates to true.
        assertEquals(JsonObject(emptyMap()), ContactCardFilter().toFilter().toJson())
    }

    @Test
    fun eachConditionAloneSendsOnlyItsOwnKey() {
        val conditions =
            mapOf(
                "inAddressBook" to ContactCardFilter(inAddressBook = "b1"),
                "uid" to ContactCardFilter(uid = "b1"),
                "hasMember" to ContactCardFilter(hasMember = "b1"),
                "kind" to ContactCardFilter(kind = CardKind("b1")),
                "text" to ContactCardFilter(text = "b1"),
                "name" to ContactCardFilter(name = "b1"),
                "name/given" to ContactCardFilter(nameGiven = "b1"),
                "name/surname" to ContactCardFilter(nameSurname = "b1"),
                "name/surname2" to ContactCardFilter(nameSurname2 = "b1"),
                "nickname" to ContactCardFilter(nickname = "b1"),
                "organization" to ContactCardFilter(organization = "b1"),
                "email" to ContactCardFilter(email = "b1"),
                "phone" to ContactCardFilter(phone = "b1"),
                "onlineService" to ContactCardFilter(onlineService = "b1"),
                "address" to ContactCardFilter(address = "b1"),
                "note" to ContactCardFilter(note = "b1"),
            )
        conditions.forEach { (key, filter) ->
            assertEquals(parseObject("""{"$key": "b1"}"""), filter.toFilter().toJson(), key)
        }
    }

    @Test
    fun datesAreUtcWithoutAZeroFraction() {
        // RFC 8620, section 1.4: a UTCDate ends in Z and omits a zero fraction of a second.
        val dates =
            ContactCardFilter(
                createdBefore = Instant.parse("2010-10-10T10:10:10.120Z"),
                createdAfter = Instant.parse("2010-10-10T12:10:10+02:00"),
                updatedBefore = Instant.parse("2010-10-10T10:10:10.000Z"),
                updatedAfter = Instant.parse("2010-10-10T10:10:10.5Z"),
            )
        assertEquals(
            parseObject(
                """{"createdBefore": "2010-10-10T10:10:10.12Z", "createdAfter": "2010-10-10T10:10:10Z",
                "updatedBefore": "2010-10-10T10:10:10Z", "updatedAfter": "2010-10-10T10:10:10.5Z"}""",
            ),
            dates.toFilter().toJson(),
        )
    }

    @Test
    fun conditionsCombineWithFilterOperators() {
        val filter =
            allOf(
                ContactCardFilter(inAddressBook = PERSONAL).toFilter(),
                noneOf(ContactCardFilter(kind = CardKind.Group).toFilter()),
            )
        val query = QueryMethod(ContactCardType, "a0x9", filter, listOf(JmapSort("name/surname")))
        assertEquals(
            parseObject(
                """{"accountId": "a0x9", "filter": {"operator": "AND", "conditions": [
                {"inAddressBook": "$PERSONAL"}, {"operator": "NOT", "conditions": [{"kind": "group"}]}]},
                "sort": [{"property": "name/surname", "isAscending": true}]}""",
            ),
            query.arguments(),
        )
    }
}
