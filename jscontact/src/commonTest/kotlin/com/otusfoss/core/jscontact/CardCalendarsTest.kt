package com.otusfoss.core.jscontact

import com.otusfoss.core.jscontact.RoundTrip.assertEntriesRoundTrip
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CardCalendarsTest {
    @Test
    fun calendarsOfFigure29RoundTrip() {
        val (calendars, encoded) = assertEntriesRoundTrip(Calendar.serializer(), FIGURE_29, "calendars")
        assertEquals(
            mapOf(
                "calA" to Calendar(CalendarKind.Calendar, "webcal://calendar.example.com/calA.ics"),
                "project-a" to Calendar(CalendarKind.FreeBusy, "https://calendar.example.com/busy/project-a"),
            ),
            calendars,
        )
        assertEquals(JsonPrimitive("Calendar"), encoded.getValue("calA").jsonObject["@type"])
    }

    @Test
    fun aCalendarIsAResource() {
        val resource: ContactResource =
            Calendar(
                kind = CalendarKind.Calendar,
                uri = "https://calendar.example.com/calA.ics",
                mediaType = "text/calendar",
                contexts = mapOf(ContactContext.Work to true),
                pref = 1,
                label = "Team",
            )
        assertEquals("https://calendar.example.com/calA.ics", resource.uri)
        assertEquals("text/calendar", resource.mediaType)
        assertEquals(mapOf(ContactContext.Work to true), resource.contexts)
        assertEquals(1, resource.pref)
        assertEquals("Team", resource.label)
    }

    @Test
    fun schedulingAddressesOfFigure30RoundTrip() {
        val (addresses, encoded) =
            assertEntriesRoundTrip(SchedulingAddress.serializer(), FIGURE_30, "schedulingAddresses")
        assertEquals(mapOf("sched1" to SchedulingAddress("mailto:janedoe@example.com")), addresses)
        assertEquals(JsonPrimitive("SchedulingAddress"), encoded.getValue("sched1").jsonObject["@type"])
    }

    @Test
    fun vendorKindsAndUnknownPropertiesRoundTrip() {
        val json =
            """{"kind": "example.com:tasks", "uri": "https://calendar.example.com/tasks",
               "example.com:color": "#3366ff", "futureProperty": [1, 2]}"""
        val calendar = RoundTrip.assertRoundTrip(Calendar.serializer(), JsContactJson.parseToJsonElement(json))
        assertEquals(CalendarKind("example.com:tasks"), calendar.kind)
        assertEquals(setOf("example.com:color", "futureProperty"), calendar.extra.keys)
    }

    @Test
    fun aCalendarTypedAsTheReservedResourceTypeFails() {
        assertFailsWith<SerializationException> {
            JsContactJson.decodeFromString(
                Calendar.serializer(),
                """{"@type": "Resource", "kind": "calendar", "uri": "https://calendar.example.com/a.ics"}""",
            )
        }
    }
}

private const val FIGURE_29 = """
   "calendars": {
     "calA": {
       "kind": "calendar",
       "uri": "webcal://calendar.example.com/calA.ics"
     },
     "project-a": {
       "kind": "freeBusy",
       "uri": "https://calendar.example.com/busy/project-a"
     }
   }
"""

private const val FIGURE_30 = """
   "schedulingAddresses": {
     "sched1": {
       "uri": "mailto:janedoe@example.com"
     }
   }
"""
