package com.otus.jscontact

import com.otus.jscontact.vcard.CRLF
import com.otus.jscontact.vcard.folded
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VCardWriteTest {
    @Test
    fun writtenCardsReadBackUnchanged() {
        val cards = listOf(PERSON, GROUP, EMAIL_ONLY, PHONE_ONLY, ORGANIZATION_ONLY)

        assertEquals(VCardImport(cards, failures = emptyList()), VCard.parse(VCard.write(cards)))
    }

    @Test
    fun writingUsesVCard4CrlfAndFoldsAt75Octets() {
        val written = VCard.write(listOf(PERSON))
        val lines = written.removeSuffix("\r\n").split("\r\n")

        assertTrue(written.startsWith("BEGIN:VCARD\r\nVERSION:4.0\r\n"))
        assertTrue(written.endsWith("END:VCARD\r\n"))
        assertTrue('\n' !in written.replace("\r\n", ""), "Every line break is CRLF")
        assertTrue(lines.all { it.encodeToByteArray().size <= 75 }, "No line is longer than 75 octets")
        assertTrue(lines.any { it.startsWith(" ") }, "The long URL and note are folded")
    }

    @Test
    fun writingEscapesTextAndWritesTypesPrefAndLabels() {
        val lines = VCard.write(listOf(PERSON)).split("\r\n")

        assertTrue("FN:Dr. Jane Doe\\, PhD" in lines)
        assertTrue("N:Doe;Jane;;Dr.;PhD" in lines)
        assertTrue("ORG:ABC\\; Inc.;Research" in lines)
        assertTrue("NICKNAME:Jay\\, the second" in lines)
        assertTrue("email1.EMAIL;TYPE=work;PREF=1:jane@example.com" in lines)
        assertTrue("email1.X-ABLABEL:Office" in lines)
        assertTrue("tel1.TEL;TYPE=cell,text,work;PREF=1:tel:+1-555-555-5555" in lines)
        assertTrue("tel1.X-ABLABEL:Desk" in lines)
        assertTrue("BDAY:19850412" in lines)
        assertTrue("ANNIVERSARY:--0601" in lines)
        assertTrue("ROLE:Project lead" in lines)
    }

    @Test
    fun aCardWithoutANameGetsADerivedFullName() {
        val lines = VCard.write(listOf(ORGANIZATION_ONLY, PHONE_ONLY)).split("\r\n")

        assertEquals(listOf("FN;DERIVED=TRUE:ACME", "FN;DERIVED=TRUE:+1 555"), lines.filter { it.startsWith("FN") })
    }

    @Test
    fun aGenerationIsAlsoWrittenAsACredential() {
        val name =
            Name(
                components =
                    listOf(
                        NameComponent("Doe", NameComponentKind.Surname),
                        NameComponent("John", NameComponentKind.Given),
                        NameComponent("Jr.", NameComponentKind.Generation),
                    ),
                isOrdered = false,
                full = "John Doe Jr.",
            )
        val card = Card(kind = CardKind.Individual, name = name)

        val written = VCard.write(listOf(card))

        assertTrue("N:Doe;John;;;Jr.;;Jr.\r\n" in written)
        assertEquals(listOf(card), VCard.parse(written).cards)
    }

    @Test
    fun lineBreaksInUrisKindsAndTextCannotStartAnotherContentLine() {
        val card =
            Card(
                kind = CardKind("group\r\nFN:Injected"),
                name = Name(full = "Eve\rMallory"),
                members = mapOf("urn:uuid:1\nFN:Member" to true),
                media = mapOf("m1" to Media(kind = MediaKind.Photo, uri = "https://a.example/p.png\r\nNOTE:Photo")),
                links = mapOf("l1" to Link("https://a.example/\nEND:VCARD\nBEGIN:VCARD\nFN:X")),
            )

        val written = VCard.write(listOf(card))
        val read = VCard.parse(written)

        assertTrue(written.replace("\r\n", "").none { it == '\r' || it == '\n' }, "Every line break ends a line")
        assertEquals(emptyList(), read.failures)
        val single = read.cards.single()
        assertEquals(CardKind.Individual, single.kind)
        assertEquals("Eve\nMallory", single.name?.full)
        assertEquals(mapOf("urn:uuid:1%0AFN:Member" to true), single.members)
        assertEquals(
            "https://a.example/p.png%0D%0ANOTE:Photo",
            single.media
                ?.values
                ?.single()
                ?.uri,
        )
        assertEquals(
            "https://a.example/%0AEND:VCARD%0ABEGIN:VCARD%0AFN:X",
            single.links
                ?.values
                ?.single()
                ?.uri,
        )
    }

    @Test
    fun aCarriageReturnInANoteCannotAddAProperty() {
        val card = Card(name = Name(full = "Jane"), notes = mapOf("n1" to Note("a\rEMAIL:x@evil.example")))

        val read = VCard.parse(VCard.write(listOf(card))).cards.single()

        assertNull(read.emails)
        assertEquals(mapOf("n1" to Note("a\nEMAIL:x@evil.example")), read.notes)
    }

    private companion object {
        val WORK = mapOf(ContactContext.Work to true)

        val PERSON =
            Card(
                kind = CardKind.Individual,
                uid = "urn:uuid:4fbe8971-0bc3-424c-9c26-36c3e1eff6b1",
                name =
                    Name(
                        components =
                            listOf(
                                NameComponent("Doe", NameComponentKind.Surname),
                                NameComponent("Jane", NameComponentKind.Given),
                                NameComponent("Dr.", NameComponentKind.Title),
                                NameComponent("PhD", NameComponentKind.Credential),
                            ),
                        isOrdered = false,
                        full = "Dr. Jane Doe, PhD",
                    ),
                nicknames = mapOf("k1" to Nickname("JD"), "k2" to Nickname("Jay, the second")),
                organizations = mapOf("o1" to Organization("ABC; Inc.", units = listOf(OrgUnit("Research")))),
                titles =
                    mapOf(
                        "t1" to Title("Scientist", kind = TitleKind.Title),
                        "t2" to Title("Project lead", kind = TitleKind.Role),
                    ),
                emails =
                    mapOf(
                        "e1" to EmailAddress("jane@example.com", contexts = WORK, pref = 1, label = "Office"),
                        "e2" to EmailAddress("jane@home.example.com", contexts = mapOf(ContactContext.Private to true)),
                    ),
                phones =
                    mapOf(
                        "p1" to
                            Phone(
                                "tel:+1-555-555-5555",
                                features = mapOf(PhoneFeature.Mobile to true, PhoneFeature.Text to true),
                                contexts = WORK,
                                pref = 1,
                                label = "Desk",
                            ),
                    ),
                addresses =
                    mapOf(
                        "a1" to
                            Address(
                                components =
                                    listOf(
                                        AddressComponent("54321 Oak St", AddressComponentKind.Name),
                                        AddressComponent("Reston", AddressComponentKind.Locality),
                                        AddressComponent("VA", AddressComponentKind.Region),
                                        AddressComponent("20190", AddressComponentKind.Postcode),
                                        AddressComponent("USA", AddressComponentKind.Country),
                                    ),
                                countryCode = "US",
                                coordinates = "geo:38.95,-77.35",
                                timeZone = "America/New_York",
                                contexts = WORK,
                                full = "54321 Oak St\nReston, VA 20190\nUSA",
                                pref = 1,
                            ),
                    ),
                anniversaries =
                    mapOf(
                        "d1" to Anniversary(AnniversaryKind.Birth, PartialDate(year = 1985, month = 4, day = 12)),
                        "d2" to Anniversary(AnniversaryKind.Wedding, PartialDate(month = 6, day = 1)),
                    ),
                notes = mapOf("n1" to Note("Line one\nLine two; with \\ a backslash, and ".repeat(3) + "é".repeat(40))),
                media =
                    mapOf(
                        "m1" to
                            Media(
                                kind = MediaKind.Photo,
                                uri = "data:image/png;base64,iVBORw0KGgo=",
                                mediaType = "image/png",
                            ),
                    ),
                links = mapOf("l1" to Link("https://example.com/" + "a".repeat(100), label = "Blog")),
            )

        val GROUP =
            Card(
                kind = CardKind.Group,
                uid = "urn:uuid:ab4310aa-fa43-11e9-8f0b-362b9e155667",
                name = Name(full = "The Doe family"),
                members =
                    mapOf(
                        "urn:uuid:4fbe8971-0bc3-424c-9c26-36c3e1eff6b1" to true,
                        "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af" to true,
                    ),
            )

        val EMAIL_ONLY =
            Card(
                kind = CardKind.Individual,
                emails = mapOf("e1" to EmailAddress("someone@example.com")),
            )

        val PHONE_ONLY = Card(kind = CardKind.Individual, phones = mapOf("p1" to Phone("+1 555")))

        val ORGANIZATION_ONLY = Card(kind = CardKind.Org, organizations = mapOf("o1" to Organization("ACME")))
    }
}
