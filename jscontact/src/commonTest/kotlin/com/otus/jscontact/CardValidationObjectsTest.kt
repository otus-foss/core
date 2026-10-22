package com.otus.jscontact

import kotlin.test.Test

class CardValidationObjectsTest {
    @Test
    fun aNameNeedsComponentsOrFull() {
        CardJson.assertProblemAt("/name", """"name": {}""")
        CardJson.assertProblemAt("/name/components", """"name": {"components": []}""")
        CardJson.assertProblemAt(
            "/name/components",
            """"name": {"components": [{"kind": "separator", "value": " "}], "isOrdered": true}""",
        )
    }

    @Test
    fun separatorsNeedOrderedComponents() {
        CardJson.assertProblemAt("/name/components/1/kind", """"name": {"components": [$GIVEN, $DASH, $SURNAME]}""")
        CardJson.assertProblemAt(
            "/name/defaultSeparator",
            """"name": {"components": [$GIVEN], "defaultSeparator": " "}""",
        )
        CardJson.assertProblemAt(
            "/name/defaultSeparator",
            """"name": {"full": "A", "defaultSeparator": " ", "isOrdered": true}""",
        )
        CardJson.assertProblemAt(
            "/addresses/a/components/1/kind",
            """"addresses": {"a": {"components": [{"kind": "number", "value": "1"}, $DASH,
               {"kind": "name", "value": "Main"}]}}""",
        )
        CardJson.assertValid(
            """"name": {"components": [$GIVEN, $DASH, $SURNAME], "defaultSeparator": " ", "isOrdered": true}""",
        )
    }

    @Test
    fun twoSeparatorsAreNeverAdjacent() {
        CardJson.assertProblemAt(
            "/name/components/2",
            """"name": {"components": [$GIVEN, $DASH, $DASH, $SURNAME], "isOrdered": true}""",
        )
    }

    @Test
    fun aPhoneticValueNeedsAScriptOrSystemOnItsParent() {
        CardJson.assertProblemAt(
            "/name/components/0/phonetic",
            """"name": {"components": [{"kind": "given", "value": "John", "phonetic": "/ˈdʒɑːn/"}]}""",
        )
        CardJson.assertProblemAt(
            "/addresses/a/components/0/phonetic",
            """"addresses": {"a": {"components": [{"kind": "name", "value": "Main", "phonetic": "meɪn"}]}}""",
        )
        CardJson.assertProblemAt("/name/phoneticScript", """"name": {"full": "A", "phoneticScript": "Latin"}""")
        // RFC 9553, figure 1.
        CardJson.assertValid(
            """"name": {"components": [{"kind": "given", "value": "John", "phonetic": "/ˈdʒɑːn/"},
               {"kind": "surname", "value": "Smith", "phonetic": "/smɪθ/"}], "phoneticSystem": "ipa"}""",
        )
    }

    @Test
    fun sortAsKeysNameKindsOfTheComponents() {
        CardJson.assertProblemAt(
            "/name/sortAs/surname",
            """"name": {"components": [$GIVEN], "sortAs": {"surname": "B"}}""",
        )
        CardJson.assertProblemAt("/name/sortAs", """"name": {"full": "A", "sortAs": {"given": "A"}}""")
        // RFC 9553, figure 19.
        CardJson.assertValid(
            """"name": {"components": [{"kind": "given", "value": "Robert"}, {"kind": "given2", "value": "Pau"},
               {"kind": "surname", "value": "Shou Chang"}], "sortAs": {"surname": "Pau Shou Chang", "given": "Robert"},
               "isOrdered": true}""",
        )
    }

    @Test
    fun organizationsAndSpeakToAsNeedOneOfTheirProperties() {
        CardJson.assertProblemAt("/organizations/o", """"organizations": {"o": {}}""")
        CardJson.assertProblemAt("/organizations/o/units", """"organizations": {"o": {"units": []}}""")
        CardJson.assertProblemAt("/speakToAs", """"speakToAs": {}""")
        CardJson.assertProblemAt("/onlineServices/s", """"onlineServices": {"s": {"service": "Mastodon"}}""")
        CardJson.assertProblemAt("/notes/n/author", """"notes": {"n": {"note": "x", "author": {}}}""")
    }

    @Test
    fun anAddressNeedsWhereItIs() {
        CardJson.assertProblemAt("/addresses/a", """"addresses": {"a": {"contexts": {"work": true}, "pref": 1}}""")
        CardJson.assertProblemAt(
            "/anniversaries/a/place",
            """"anniversaries": {"a": {"kind": "birth", "date": {"year": 2000}, "place": {}}}""",
        )
        CardJson.assertProblemAt("/addresses/a/countryCode", """"addresses": {"a": {"countryCode": "USA"}}""")
        CardJson.assertProblemAt("/addresses/a/coordinates", """"addresses": {"a": {"coordinates": "40.7,-74.0"}}""")
        CardJson.assertProblemAt(
            "/addresses/a/coordinates",
            """"addresses": {"a": {"coordinates": "geo:40.7, -74.0"}}""",
        )
        CardJson.assertValid(
            """"addresses": {"a": {"coordinates": "geo:37.786971,-122.399677", "countryCode": "US"}}""",
            """"addresses": {"a": {"timeZone": "Europe/Vienna"}}""",
        )
    }

    @Test
    fun mediaNeedsAUriOrABlobWithAMediaType() {
        CardJson.assertProblemAt("/media/m", """"media": {"m": {"kind": "photo"}}""")
        CardJson.assertProblemAt("/media/m/mediaType", """"media": {"m": {"kind": "photo", "blobId": "b1"}}""")
        CardJson.assertProblemAt(
            "/media/m/blobId",
            """"media": {"m": {"kind": "photo", "blobId": "b 1", "mediaType": "image/png"}}""",
        )
        CardJson.assertValid(""""media": {"m": {"kind": "photo", "blobId": "b1", "mediaType": "image/png"}}""")
    }

    @Test
    fun listAsIsAboveZero() {
        CardJson.assertProblemAt(
            "/directories/d/listAs",
            """"directories": {"d": {"kind": "entry", "uri": "https://example.com", "listAs": 0}}""",
        )
        CardJson.assertProblemAt(
            "/personalInfo/p/listAs",
            """"personalInfo": {"p": {"kind": "hobby", "value": "x", "listAs": 0}}""",
        )
        CardJson.assertValid(""""personalInfo": {"p": {"kind": "hobby", "value": "x", "listAs": 1}}""")
    }

    @Test
    fun partialDatesAreDatesThatExist() {
        val cases =
            listOf(
                "/date" to "{}",
                "/date/year" to """{"year": -1}""",
                "/date/month" to """{"year": 2000, "month": 13}""",
                "/date/month" to """{"month": 4}""",
                "/date/day" to """{"day": 1}""",
                "/date/day" to """{"year": 2023, "month": 4, "day": 31}""",
                "/date/day" to """{"year": 1900, "month": 2, "day": 29}""",
                "/date/calendarScale" to """{"year": 2000, "calendarScale": "Hebrew"}""",
            )
        cases.forEach { (path, date) ->
            CardJson.assertProblemAt(
                "/anniversaries/a$path",
                """"anniversaries": {"a": {"kind": "birth", "date": $date}}""",
            )
        }
        CardJson.assertValid(
            """"anniversaries": {"a": {"kind": "birth", "date": {"year": 2000, "month": 2, "day": 29}}}""",
            """"anniversaries": {"a": {"kind": "birth", "date": {"month": 2, "day": 29}}}""",
            """"anniversaries": {"a": {"kind": "birth", "date": {"year": 1953, "calendarScale": "hebrew"}}}""",
        )
    }
}

private const val GIVEN = """{"kind": "given", "value": "A"}"""

private const val SURNAME = """{"kind": "surname", "value": "B"}"""

private const val DASH = """{"kind": "separator", "value": "-"}"""
