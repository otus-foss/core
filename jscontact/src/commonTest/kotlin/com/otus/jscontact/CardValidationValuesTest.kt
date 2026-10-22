package com.otus.jscontact

import kotlin.test.Test

class CardValidationValuesTest {
    @Test
    fun prefIsFromOneTo100() {
        CardJson.assertProblemAt("/emails/e/pref", """"emails": {"e": {"address": "a@example.com", "pref": 0}}""")
        CardJson.assertProblemAt("/nicknames/n/pref", """"nicknames": {"n": {"name": "J", "pref": 101}}""")
        CardJson.assertProblemAt(
            "/speakToAs/pronouns/p/pref",
            """"speakToAs": {"pronouns": {"p": {"pronouns": "they/them", "pref": 0}}}""",
        )
        CardJson.assertProblemAt("/links/l/pref", """"links": {"l": {"uri": "https://example.com", "pref": 0}}""")
        CardJson.assertValid(
            """"emails": {"e": {"address": "a@example.com", "pref": 1}}""",
            """"addresses": {"a": {"full": "x", "pref": 100}}""",
        )
    }

    @Test
    fun idMapKeysAreIds() {
        CardJson.assertProblemAt("/emails/bad key", """"emails": {"bad key": {"address": "a@example.com"}}""")
        CardJson.assertProblemAt("/titles/", """"titles": {"": {"name": "x"}}""")
        val tooLong = "a".repeat(256)
        CardJson.assertProblemAt("/notes/$tooLong", """"notes": {"$tooLong": {"note": "x"}}""")
        CardJson.assertValid(
            """"notes": {"${"a".repeat(255)}": {"note": "x"}}""",
            """"relatedTo": {"8cacdfb7d1ffdb59@example.com": {}}""",
        )
    }

    @Test
    fun registeredValuesThatDifferOnlyInCaseAreInvalid() {
        val cases =
            mapOf(
                "/kind" to """"kind": "Individual"""",
                "/name/components/0/kind" to """"name": {"components": [{"kind": "Given", "value": "J"}]}""",
                "/name/phoneticSystem" to """"name": {"full": "x", "phoneticSystem": "IPA"}""",
                "/relatedTo/u2/relation/Friend" to """"relatedTo": {"u2": {"relation": {"Friend": true}}}""",
                "/speakToAs/grammaticalGender" to """"speakToAs": {"grammaticalGender": "Neuter"}""",
                "/titles/t/kind" to """"titles": {"t": {"name": "x", "kind": "Role"}}""",
                "/emails/e/contexts/Work" to
                    """"emails": {"e": {"address": "a@example.com", "contexts": {"Work": true}}}""",
                "/phones/p/features/Mobile" to """"phones": {"p": {"number": "1", "features": {"Mobile": true}}}""",
                "/calendars/c/kind" to """"calendars": {"c": {"kind": "freebusy", "uri": "https://example.com"}}""",
                "/addresses/a/components/0/kind" to
                    """"addresses": {"a": {"components": [{"kind": "PostOfficeBox", "value": "1"}]}}""",
                "/directories/d/kind" to """"directories": {"d": {"kind": "Entry", "uri": "https://example.com"}}""",
                "/links/l/kind" to """"links": {"l": {"kind": "Contact", "uri": "https://example.com"}}""",
                "/media/m/kind" to """"media": {"m": {"kind": "Photo", "uri": "https://example.com/p.jpg"}}""",
                "/anniversaries/a/kind" to """"anniversaries": {"a": {"kind": "Birth", "date": {"year": 2000}}}""",
                "/personalInfo/p/kind" to """"personalInfo": {"p": {"kind": "Hobby", "value": "x"}}""",
                "/personalInfo/p/level" to
                    """"personalInfo": {"p": {"kind": "hobby", "value": "x", "level": "High"}}""",
            )
        cases.forEach { (path, members) -> CardJson.assertProblemAt(path, members) }
    }

    @Test
    fun unknownValuesPassButVendorValuesNeedAVendorPrefix() {
        CardJson.assertValid(
            """"kind": "example.com:baz"""",
            """"kind": "robot"""",
            """"phones": {"p": {"number": "1", "features": {"example.com:satellite": true, "hologram": true}}}""",
            """"cryptoKeys": {"k": {"uri": "https://example.com/k", "kind": "example.com:pgp"}}""",
            """"kind": "example.com:😀"""",
            """"kind": "😀.example:robot"""",
        )
        for (kind in listOf("example.com:", "-example.com:x", "example.com:a/b", "example..com:x", ":x")) {
            CardJson.assertProblemAt("/kind", """"kind": "$kind"""")
        }
    }

    @Test
    fun billingAndDeliveryAreContextsOfAddressesOnly() {
        CardJson.assertProblemAt(
            "/phones/p/contexts/billing",
            """"phones": {"p": {"number": "1", "contexts": {"billing": true}}}""",
        )
        CardJson.assertProblemAt(
            "/organizations/o/contexts/delivery",
            """"organizations": {"o": {"name": "A", "contexts": {"delivery": true}}}""",
        )
        CardJson.assertValid(""""addresses": {"a": {"full": "x", "contexts": {"billing": true, "delivery": true}}}""")
    }

    @Test
    fun uriPropertiesHoldUris() {
        val cases =
            mapOf(
                "/calendars/c/uri" to """"calendars": {"c": {"kind": "calendar", "uri": "not a uri"}}""",
                "/links/l/uri" to """"links": {"l": {"uri": "https://example.com/a b"}}""",
                "/cryptoKeys/k/uri" to """"cryptoKeys": {"k": {"uri": "https://example.com/%zz"}}""",
                "/onlineServices/s/uri" to """"onlineServices": {"s": {"uri": "alice"}}""",
                "/schedulingAddresses/s/uri" to
                    """"schedulingAddresses": {"s": {"uri": "mailto:jürgen@example.com"}}""",
                "/directories/d/uri" to """"directories": {"d": {"kind": "entry", "uri": ""}}""",
                "/media/m/uri" to """"media": {"m": {"kind": "photo", "uri": "1photo.jpg"}}""",
            )
        cases.forEach { (path, members) -> CardJson.assertProblemAt(path, members) }
        CardJson.assertValid(
            """"links": {"l": {"uri": "https://example.com/Jean%20Dupont.vcf?x=1#top"}}""",
            """"schedulingAddresses": {"s": {"uri": "mailto:j%C3%BCrgen@example.com"}}""",
        )
    }

    @Test
    fun languagesAreLanguageTags() {
        CardJson.assertProblemAt("/language", """"language": "en_US"""")
        CardJson.assertProblemAt(
            "/preferredLanguages/l/language",
            """"preferredLanguages": {"l": {"language": "123"}}""",
        )
        CardJson.assertValid(
            """"language": "zh-Hant-TW"""",
            """"language": "i-klingon"""",
            """"language": "x-whatever"""",
        )
    }

    @Test
    fun emailAddressesAreAddrSpecs() {
        val invalid = listOf("not-an-address", "a@", "Jane <jane@example.com>", "a b@example.com", "a@b@example.com")
        for (address in invalid) {
            CardJson.assertProblemAt("/emails/e/address", """"emails": {"e": {"address": "$address"}}""")
        }
        CardJson.assertValid(
            """"emails": {"e": {"address": "\"john doe\"@example.com"}}""",
            """"emails": {"e": {"address": "jürgen@example.com"}}""",
            """"emails": {"e": {"address": "jane@[192.0.2.1]"}}""",
        )
    }
}
