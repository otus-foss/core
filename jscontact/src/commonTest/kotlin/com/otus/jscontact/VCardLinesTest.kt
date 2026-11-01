package com.otus.jscontact

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class VCardLinesTest {
    @Test
    fun aLongQuotedPrintableValueIsJoinedInLinearTime() {
        val parts = 100_000
        val text =
            (
                listOf("BEGIN:VCARD", "VERSION:2.1", "FN:Jane Doe", "NOTE;ENCODING=QUOTED-PRINTABLE:Caf=C3=A9=") +
                    List(parts) { "aa=" } +
                    listOf("end", "END:VCARD")
            ).joinToString("\r\n")

        val (parsed, elapsed) = measureTimedValue { VCard.parse(text) }

        assertEquals(mapOf("n1" to Note("Café" + "aa".repeat(parts) + "end")), parsed.cards.single().notes)
        assertTrue(elapsed < 2.seconds, "Parsing took $elapsed")
    }
}
