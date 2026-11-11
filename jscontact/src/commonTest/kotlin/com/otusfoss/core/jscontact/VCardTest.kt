package com.otusfoss.core.jscontact

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class VCardTest {
    @Test
    fun aVCard3ReadsFoldedLinesQuotedPrintableAndTypeLists() {
        val text =
            listOf(
                "BEGIN:VCARD",
                "VERSION:3.0",
                "N:Gump;Forrest;;Mr.;",
                "FN:Forrest",
                "\t Gump",
                "ORG:Bubba Gump Shrimp Co.;Sales",
                "TITLE:Shrimp Man",
                "item1.EMAIL;type=INTERNET;type=WORK;type=pref:forrest@example.com",
                "item1.X-ABLabel:Office",
                "TEL;TYPE=WORK,VOICE:(111) 555-1212",
                "tel;type=CELL:(404) 555-1212",
                "ADR;TYPE=HOME:;;42 Plantation St.;Baytown;LA;30314;United States of America",
                "NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:Caf=C3=A9 au lait",
                "URL:http://example.com/a-very-long-",
                " path",
                "BDAY:1944-06-06",
                "END:VCARD",
            ).joinToString("\r\n")

        val card = VCard.parse(text).cards.single()

        assertEquals(CardKind.Individual, card.kind)
        assertEquals(
            Name(
                components =
                    listOf(
                        NameComponent("Gump", NameComponentKind.Surname),
                        NameComponent("Forrest", NameComponentKind.Given),
                        NameComponent("Mr.", NameComponentKind.Title),
                    ),
                isOrdered = false,
                full = "Forrest Gump",
            ),
            card.name,
        )
        assertEquals(
            mapOf("o1" to Organization("Bubba Gump Shrimp Co.", units = listOf(OrgUnit("Sales")))),
            card.organizations,
        )
        assertEquals(mapOf("t1" to Title("Shrimp Man", kind = TitleKind.Title)), card.titles)
        assertEquals(
            mapOf("e1" to EmailAddress("forrest@example.com", contexts = WORK, pref = 1, label = "Office")),
            card.emails,
        )
        assertEquals(
            mapOf(
                "p1" to Phone("(111) 555-1212", features = mapOf(PhoneFeature.Voice to true), contexts = WORK),
                "p2" to Phone("(404) 555-1212", features = mapOf(PhoneFeature.Mobile to true)),
            ),
            card.phones,
        )
        assertEquals(
            mapOf(
                "a1" to
                    Address(
                        components =
                            listOf(
                                AddressComponent("42 Plantation St.", AddressComponentKind.Name),
                                AddressComponent("Baytown", AddressComponentKind.Locality),
                                AddressComponent("LA", AddressComponentKind.Region),
                                AddressComponent("30314", AddressComponentKind.Postcode),
                                AddressComponent("United States of America", AddressComponentKind.Country),
                            ),
                        contexts = mapOf(ContactContext.Private to true),
                    ),
            ),
            card.addresses,
        )
        assertEquals(mapOf("n1" to Note("Café au lait")), card.notes)
        assertEquals(mapOf("l1" to Link("http://example.com/a-very-long-path")), card.links)
        assertEquals(
            mapOf("d1" to Anniversary(AnniversaryKind.Birth, PartialDate(year = 1944, month = 6, day = 6))),
            card.anniversaries,
        )
    }

    @Test
    fun aVCard21QuotedPrintableValueContinuesAfterASoftLineBreak() {
        val card =
            card(
                "FN:Jane Doe",
                "NOTE;ENCODING=QUOTED-PRINTABLE:Line one=0ALine t=",
                "wo",
            )

        assertEquals(mapOf("n1" to Note("Line one\nLine two")), card.notes)
    }

    @Test
    fun aVCard4ReadsPrefUidAndBirthday() {
        val text =
            listOf(
                "BEGIN:VCARD",
                "VERSION:4.0",
                "UID:urn:uuid:4fbe8971-0bc3-424c-9c26-36c3e1eff6b1",
                "FN:Jane Doe",
                "EMAIL;TYPE=work;PREF=1:jane@work.example.com",
                "EMAIL;TYPE=home;PREF=2:jane@example.com",
                "TEL;VALUE=uri;PREF=1;TYPE=\"voice,home\":tel:+1-555-555-5555",
                "BDAY:--0412",
                "END:VCARD",
            ).joinToString("\n")

        val card = VCard.parse(text).cards.single()

        assertEquals("urn:uuid:4fbe8971-0bc3-424c-9c26-36c3e1eff6b1", card.uid)
        assertEquals(Name(full = "Jane Doe"), card.name)
        assertEquals(
            mapOf(
                "e1" to EmailAddress("jane@work.example.com", contexts = WORK, pref = 1),
                "e2" to EmailAddress("jane@example.com", contexts = mapOf(ContactContext.Private to true), pref = 2),
            ),
            card.emails,
        )
        assertEquals(
            Phone(
                "tel:+1-555-555-5555",
                features = mapOf(PhoneFeature.Voice to true),
                contexts = mapOf(ContactContext.Private to true),
                pref = 1,
            ),
            card.phones?.getValue("p1"),
        )
        assertEquals(
            mapOf("d1" to Anniversary(AnniversaryKind.Birth, PartialDate(month = 4, day = 12))),
            card.anniversaries,
        )
    }

    @Test
    fun birthdaysReadInEachDateForm() {
        val forms: Map<String, AnniversaryDate> =
            mapOf(
                "BDAY:--0412" to PartialDate(month = 4, day = 12),
                "BDAY:--04-12" to PartialDate(month = 4, day = 12),
                "BDAY:19850412" to PartialDate(year = 1985, month = 4, day = 12),
                "BDAY:1985-04-12" to PartialDate(year = 1985, month = 4, day = 12),
                "BDAY:1985-04-12T10:00:00" to PartialDate(year = 1985, month = 4, day = 12),
                "BDAY:1985" to PartialDate(year = 1985),
                "BDAY:19961022T140000Z" to Timestamp(Instant.parse("1996-10-22T14:00:00Z")),
                "BDAY;X-APPLE-OMIT-YEAR=1604:1604-03-15" to PartialDate(month = 3, day = 15),
            )

        forms.forEach { (line, date) ->
            assertEquals(Anniversary(AnniversaryKind.Birth, date), card("FN:Jane", line).anniversaries?.get("d1"), line)
        }
        assertNull(card("FN:Jane", "BDAY;VALUE=text:circa 1800").anniversaries)
    }

    @Test
    fun aGroupCardReadsItsMembers() {
        val card =
            card(
                "KIND:group",
                "FN:The Doe family",
                "MEMBER:urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af",
                "MEMBER:urn:uuid:b8767877-b4a1-4c70-9acc-505d3819e519",
            )

        assertEquals(CardKind.Group, card.kind)
        assertEquals(
            mapOf(
                "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af" to true,
                "urn:uuid:b8767877-b4a1-4c70-9acc-505d3819e519" to true,
            ),
            card.members,
        )
    }

    @Test
    fun aCardWithoutContactDetailsFailsAtItsPosition() {
        val text =
            listOf(
                "BEGIN:VCARD",
                "VERSION:4.0",
                "FN:First",
                "END:VCARD",
                "BEGIN:VCARD",
                "VERSION:4.0",
                "UID:urn:uuid:only-an-id",
                "END:VCARD",
                "BEGIN:VCARD",
                "VERSION:4.0",
                "TEL:+1-555-0100",
                "END:VCARD",
            ).joinToString("\r\n")

        val result = VCard.parse(text)

        assertEquals(listOf(VCardFailure(2, "It has no contact details.")), result.failures)
        assertEquals(listOf(Name(full = "First"), null), result.cards.map { it.name })
        assertEquals(mapOf("p1" to Phone("+1-555-0100")), result.cards[1].phones)
    }

    @Test
    fun aByteOrderMarkBeforeTheFirstCardIsSkipped() {
        val result = VCard.parse("\uFEFFBEGIN:VCARD\r\nVERSION:3.0\r\nFN:Ada\r\nEND:VCARD\r\n")

        assertEquals(listOf(Name(full = "Ada")), result.cards.map { it.name })
        assertEquals(emptyList(), result.failures)
    }

    @Test
    fun aCardWithoutEndFails() {
        val result = VCard.parse("BEGIN:VCARD\r\nVERSION:4.0\r\nFN:Cut off\r\n")

        assertEquals(listOf(VCardFailure(1, "It has no END:VCARD line.")), result.failures)
        assertEquals(emptyList<Card>(), result.cards)
    }

    @Test
    fun escapedValuesAndStructuredValuesRead() {
        val card =
            card(
                """FN:Doe\, Jane""",
                """N:Doe\;Smith;Jane;;;""",
                """NOTE:Line one\nLine two\\ with a backslash\, and a comma""",
                """ORG:ABC\, Inc.;North American Division;Marketing""",
                """NICKNAME:JD,Jay\, the second""",
            )

        assertEquals("Doe, Jane", card.name?.full)
        assertEquals(
            listOf(
                NameComponent("Doe;Smith", NameComponentKind.Surname),
                NameComponent("Jane", NameComponentKind.Given),
            ),
            card.name?.components,
        )
        assertEquals("Line one\nLine two\\ with a backslash, and a comma", card.notes?.getValue("n1")?.note)
        assertEquals(
            Organization("ABC, Inc.", units = listOf(OrgUnit("North American Division"), OrgUnit("Marketing"))),
            card.organizations?.getValue("o1"),
        )
        assertEquals(
            mapOf("k1" to Nickname("JD"), "k2" to Nickname("Jay, the second")),
            card.nicknames,
        )
    }

    @Test
    fun aCardWithoutFnTakesItsFullNameFromN() {
        val card = card("N:Doe;Jane;Q.;Dr.;PhD")

        assertEquals("Dr. Jane Q. Doe PhD", card.name?.full)
        assertEquals(false, card.name?.isOrdered)
    }

    @Test
    fun anInlineVCard3PhotoBecomesADataUri() {
        val card = card("FN:Jane", "PHOTO;ENCODING=b;TYPE=JPEG:/9j/4AAQ", " SkZJRg==")

        assertEquals(
            mapOf("m1" to Media(kind = MediaKind.Photo, uri = "data:image/jpeg;base64,/9j/4AAQSkZJRg==")),
            card.media,
        )
    }

    /** The one Card of a vCard 4.0 entry with [lines] between BEGIN and END. */
    private fun card(vararg lines: String): Card =
        VCard
            .parse((listOf("BEGIN:VCARD", "VERSION:4.0") + lines + "END:VCARD").joinToString("\r\n"))
            .cards
            .single()

    private companion object {
        val WORK = mapOf(ContactContext.Work to true)
    }
}
