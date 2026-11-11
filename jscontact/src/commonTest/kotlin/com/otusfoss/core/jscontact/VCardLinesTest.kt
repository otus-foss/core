package com.otusfoss.core.jscontact

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

    @Test
    fun aQuotedPrintableSoftBreakKeepsTheSpaceThatStartsTheNextLine() {
        val parsed =
            VCard.parse(
                listOf(
                    "BEGIN:VCARD",
                    "VERSION:2.1",
                    "FN:Jane Doe",
                    "NOTE;ENCODING=QUOTED-PRINTABLE:Line one=",
                    " two",
                    "END:VCARD",
                ).joinToString("\r\n"),
            )

        assertEquals(mapOf("n1" to Note("Line one two")), parsed.cards.single().notes)
    }

    @Test
    fun aFoldedLineAfterAFinishedQuotedPrintableValueStillUnfolds() {
        val parsed =
            VCard.parse(
                listOf(
                    "BEGIN:VCARD",
                    "VERSION:2.1",
                    "N;ENCODING=QUOTED-PRINTABLE:Doe;Jane",
                    "NOTE:folded",
                    "  note",
                    "END:VCARD",
                ).joinToString("\n"),
            )

        assertEquals(mapOf("n1" to Note("folded note")), parsed.cards.single().notes)
    }
}
