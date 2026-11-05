package com.otus.jscontact

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VCardDatesTest {
    @Test
    fun datesWithAMonthOrDayThatDoesNotExistAreDropped() {
        val lines =
            listOf("BDAY:19900231", "BDAY:--1340", "ANNIVERSARY:2020-00-00", "BDAY:2020-13", "DEATHDATE:20230229")

        lines.forEach { line ->
            val card = card("FN:Jane", line)
            assertNull(card.anniversaries, line)
            assertEquals(emptyList(), card.validate(), line)
        }
    }

    @Test
    fun februaryTheTwentyNinthReadsInALeapYearOrWithoutAYear() {
        val dates =
            mapOf(
                "BDAY:20000229" to PartialDate(year = 2000, month = 2, day = 29),
                "BDAY:--0229" to PartialDate(month = 2, day = 29),
                "BDAY;X-APPLE-OMIT-YEAR=1604:1604-02-29" to PartialDate(month = 2, day = 29),
            )

        dates.forEach { (line, date) ->
            assertEquals(Anniversary(AnniversaryKind.Birth, date), card("FN:Jane", line).anniversaries?.get("d1"), line)
        }
    }

    /** The one Card of a vCard 4.0 entry with [lines] between BEGIN and END, with a uid so that it validates. */
    private fun card(vararg lines: String): Card =
        VCard
            .parse((listOf("BEGIN:VCARD", "VERSION:4.0", "UID:urn:uuid:1") + lines + "END:VCARD").joinToString("\r\n"))
            .cards
            .single()
}
