package com.otus.jscontact

import kotlin.test.Test
import kotlin.test.assertEquals

/** The companion constants against the IANA "JSContact Enum Values" registry (RFC 9553, section 3.7). */
class CardNamesRegistryTest {
    @Test
    fun cardKindsMatchTheRegistry() {
        assertRegistered(
            "individual group org location device application",
            listOf(
                CardKind.Individual,
                CardKind.Group,
                CardKind.Org,
                CardKind.Location,
                CardKind.Device,
                CardKind.Application,
            ).map { it.value },
        )
    }

    @Test
    fun relationTypesMatchTheRegistry() {
        assertRegistered(
            "acquaintance agent child co-resident co-worker colleague contact crush date emergency " +
                "friend kin me met muse neighbor parent sibling spouse sweetheart",
            listOf(
                RelationType.Acquaintance,
                RelationType.Agent,
                RelationType.Child,
                RelationType.CoResident,
                RelationType.CoWorker,
                RelationType.Colleague,
                RelationType.Contact,
                RelationType.Crush,
                RelationType.Date,
                RelationType.Emergency,
                RelationType.Friend,
                RelationType.Kin,
                RelationType.Me,
                RelationType.Met,
                RelationType.Muse,
                RelationType.Neighbor,
                RelationType.Parent,
                RelationType.Sibling,
                RelationType.Spouse,
                RelationType.Sweetheart,
            ).map { it.value },
        )
    }

    @Test
    fun nameComponentKindsMatchTheRegistry() {
        assertRegistered(
            "title given given2 surname surname2 credential generation separator",
            listOf(
                NameComponentKind.Title,
                NameComponentKind.Given,
                NameComponentKind.Given2,
                NameComponentKind.Surname,
                NameComponentKind.Surname2,
                NameComponentKind.Credential,
                NameComponentKind.Generation,
                NameComponentKind.Separator,
            ).map { it.value },
        )
    }

    @Test
    fun phoneticSystemsMatchTheRegistry() {
        assertRegistered(
            "ipa jyut piny",
            listOf(PhoneticSystem.Ipa, PhoneticSystem.Jyut, PhoneticSystem.Piny).map { it.value },
        )
    }

    @Test
    fun grammaticalGendersMatchTheRegistry() {
        assertRegistered(
            "animate common feminine inanimate masculine neuter",
            listOf(
                GrammaticalGender.Animate,
                GrammaticalGender.Common,
                GrammaticalGender.Feminine,
                GrammaticalGender.Inanimate,
                GrammaticalGender.Masculine,
                GrammaticalGender.Neuter,
            ).map { it.value },
        )
    }

    @Test
    fun titleKindsMatchTheRegistry() {
        assertRegistered("title role", listOf(TitleKind.Title, TitleKind.Role).map { it.value })
    }

    private fun assertRegistered(
        registered: String,
        values: List<String>,
    ) = assertEquals(registered.split(" "), values)
}
