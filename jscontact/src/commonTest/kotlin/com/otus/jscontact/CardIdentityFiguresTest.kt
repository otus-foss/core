package com.otus.jscontact

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CardIdentityFiguresTest {
    @Test
    fun relatedToFigureKeepsAnEmptyRelation() {
        val (related, _) = RoundTrip.assertEntriesRoundTrip(Relation.serializer(), FIGURE_13, "relatedTo")
        val expected =
            mapOf(
                "urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6" to Relation(mapOf(RelationType.Friend to true)),
                "8cacdfb7d1ffdb59@example.com" to Relation(emptyMap()),
            )
        assertEquals(expected, related)
    }

    @Test
    fun nicknamesFigureRoundTrips() {
        val (nicknames, _) = RoundTrip.assertEntriesRoundTrip(Nickname.serializer(), FIGURE_21, "nicknames")
        assertEquals(mapOf("k391" to Nickname("Johnny")), nicknames)
    }

    @Test
    fun organizationsFigureKeepsUnitsInHierarchyOrder() {
        val (organizations, _) = RoundTrip.assertEntriesRoundTrip(Organization.serializer(), FIGURE_22, "organizations")
        val units = listOf(OrgUnit("North American Division"), OrgUnit("Marketing"))
        assertEquals(mapOf("o1" to Organization("ABC, Inc.", units, sortAs = "ABC")), organizations)
    }

    @Test
    fun speakToAsFigureKeepsPronounsById() {
        val speakToAs =
            RoundTrip.assertRoundTrip(SpeakToAs.serializer(), RoundTrip.fragment(FIGURE_23, "speakToAs"))
        val pronouns = mapOf("k19" to Pronouns("they/them", pref = 2), "k32" to Pronouns("xe/xir", pref = 1))
        assertEquals(SpeakToAs(GrammaticalGender.Neuter, pronouns), speakToAs)
    }

    @Test
    fun titlesFigureRefersToItsOrganizationById() {
        val (titles, _) = RoundTrip.assertEntriesRoundTrip(Title.serializer(), FIGURE_24, "titles")
        val (organizations, _) = RoundTrip.assertEntriesRoundTrip(Organization.serializer(), FIGURE_24, "organizations")
        val expected =
            mapOf(
                "le9" to Title("Research Scientist", TitleKind.Title),
                "k2" to Title("Project Leader", TitleKind.Role, organizationId = "o2"),
            )
        assertEquals(expected, titles)
        assertEquals(mapOf("o2" to Organization("ABC, Inc.")), organizations)
    }

    @Test
    fun everyObjectWritesItsTypeName() {
        val written =
            listOf(
                RoundTrip.typeOf(Relation.serializer(), Relation()),
                RoundTrip.typeOf(Name.serializer(), Name(full = "Ada Lovelace")),
                RoundTrip.typeOf(NameComponent.serializer(), NameComponent("Ada", NameComponentKind.Given)),
                RoundTrip.typeOf(Nickname.serializer(), Nickname("Ada")),
                RoundTrip.typeOf(Organization.serializer(), Organization("Analytical Engines")),
                RoundTrip.typeOf(OrgUnit.serializer(), OrgUnit("Research")),
                RoundTrip.typeOf(SpeakToAs.serializer(), SpeakToAs(GrammaticalGender.Feminine)),
                RoundTrip.typeOf(Pronouns.serializer(), Pronouns("she/her")),
                RoundTrip.typeOf(Title.serializer(), Title("Mathematician")),
            )
        val expected =
            "Relation Name NameComponent Nickname Organization OrgUnit SpeakToAs Pronouns Title"
                .split(" ")
                .map { JsonPrimitive(it) }
        assertEquals(expected, written)
    }

    @Test
    fun aPresentTypeNameMustMatchTheObject() {
        val unit = JsContactJson.decodeFromString(OrgUnit.serializer(), """{"@type": "OrgUnit", "name": "Marketing"}""")
        assertEquals(OrgUnit("Marketing"), unit)
        assertFailsWith<SerializationException> {
            JsContactJson.decodeFromString(Title.serializer(), """{"@type": "Nickname", "name": "Johnny"}""")
        }
    }
}

private const val FIGURE_13 = """
"relatedTo": {
  "urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6": {
    "relation": {
      "friend": true
    }
  },
  "8cacdfb7d1ffdb59@example.com": {
    "relation": {}
  }
}
"""

private const val FIGURE_21 = """
"nicknames": {
  "k391": {
    "name": "Johnny"
  }
}
"""

private const val FIGURE_22 = """
"organizations": {
  "o1": {
    "name": "ABC, Inc.",
    "units": [
      { "name": "North American Division" },
      { "name": "Marketing" }
    ],
    "sortAs": "ABC"
  }
}
"""

private const val FIGURE_23 = """
"speakToAs": {
  "grammaticalGender": "neuter",
  "pronouns": {
    "k19": {
      "pronouns": "they/them",
      "pref": 2
    },
    "k32": {
      "pronouns": "xe/xir",
      "pref": 1
    }
  }
}
"""

private const val FIGURE_24 = """
"titles": {
  "le9": {
    "kind": "title",
    "name": "Research Scientist"
  },
  "k2": {
    "kind": "role",
    "name": "Project Leader",
    "organizationId": "o2"
  }
},
"organizations": {
  "o2": {
    "name": "ABC, Inc."
  }
}
"""
