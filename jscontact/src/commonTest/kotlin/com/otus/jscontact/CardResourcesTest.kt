package com.otus.jscontact

import com.otus.jscontact.RoundTrip.assertEntriesRoundTrip
import com.otus.jscontact.RoundTrip.decodeEntries
import com.otus.jscontact.RoundTrip.encodeEntries
import com.otus.jscontact.RoundTrip.fragment
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CardResourcesTest {
    @Test
    fun cryptoKeysWithExternalAndEmbeddedDataRoundTrip() {
        val (external, _) = assertEntriesRoundTrip(CryptoKey.serializer(), FIGURE_34, "cryptoKeys")
        assertEquals(CryptoKey("https://www.example.com/keys/jdoe.cer"), external.getValue("mykey1"))
        val (embedded, encoded) = assertEntriesRoundTrip(CryptoKey.serializer(), FIGURE_35, "cryptoKeys")
        assertEquals(EMBEDDED_KEY, embedded.getValue("mykey2").uri)
        assertEquals(JsonPrimitive("CryptoKey"), encoded.getValue("mykey2").jsonObject["@type"])
    }

    @Test
    fun directoriesKeepTheirKindAndPreference() {
        val (directories, _) = assertEntriesRoundTrip(Directory.serializer(), FIGURE_36, "directories")
        assertEquals(
            Directory(DirectoryKind.Entry, "https://dir.example.com/addrbook/jdoe/Jean%20Dupont.vcf"),
            directories.getValue("dir1"),
        )
        val dir2 = directories.getValue("dir2")
        assertEquals(DirectoryKind.Directory, dir2.kind)
        assertEquals(1, dir2.pref)
    }

    @Test
    fun aDirectoryKeepsListAsAndVendorValues() {
        val text = """"directories": {"d": {"kind": "example.com:mirror", "uri": "https://example.com/d", "listAs": 2,
            "example.com:region": {"code": "eu"}}}"""
        val directory = assertEntriesRoundTrip(Directory.serializer(), text, "directories").first.getValue("d")
        assertEquals(DirectoryKind("example.com:mirror"), directory.kind)
        assertEquals(2L, directory.listAs)
        assertEquals(setOf("example.com:region"), directory.extra.keys)
    }

    @Test
    fun linksRoundTrip() {
        val (links, _) = assertEntriesRoundTrip(Link.serializer(), FIGURE_37, "links")
        assertEquals(Link("mailto:contact@example.com", LinkKind.Contact, pref = 1), links.getValue("link3"))
    }

    @Test
    fun aLinkWithoutKindEncodesOnlyWhatIsSet() {
        val encoded = encodeEntries(Link.serializer(), mapOf("l" to Link("https://example.com/")))
        assertEquals(
            JsContactJson.parseToJsonElement("""{"@type": "Link", "uri": "https://example.com/"}"""),
            encoded.getValue("l"),
        )
    }

    @Test
    fun aResourceWithAnotherTypeIsRejected() {
        val text = """"links": {"l": {"@type": "Resource", "uri": "https://example.com/"}}"""
        assertFailsWith<SerializationException> { decodeEntries(Link.serializer(), fragment(text, "links")) }
        val key = """"cryptoKeys": {"k": {"@type": "Link", "uri": "https://example.com/k.cer"}}"""
        assertFailsWith<SerializationException> { decodeEntries(CryptoKey.serializer(), fragment(key, "cryptoKeys")) }
    }

    @Test
    fun aDirectoryWithoutKindIsRejected() {
        val text = """"directories": {"d": {"uri": "https://example.com/d"}}"""
        assertFailsWith<SerializationException> { decodeEntries(Directory.serializer(), fragment(text, "directories")) }
    }
}

// RFC 9553, Figure 34.
private const val FIGURE_34 = """
   "cryptoKeys": {
     "mykey1": {
       "uri": "https://www.example.com/keys/jdoe.cer"
     }
   }
"""

// RFC 9553, Figure 35. The RFC breaks the key data over lines "only for demonstration purposes".
private val EMBEDDED_KEY =
    listOf(
        "data:application/pgp-keys;base64,LS0tLS1CRUdJTiBSU0EgUFVC",
        "TElDIEtFWS0tLS0tCk1JSUJDZ0tDQVFFQSt4R1ovd2N6OXVnRnBQMDdOc",
        "3BvNlUxN2wwWWhGaUZweHhVNHBUazNMaWZ6OVIzenNJc3UKRVJ3dGE3K2",
        "ZXSWZ4T28yMDhldHQvamhza2lWb2RTRXQzUUJHaDRYQmlweVdvcEt3Wjk",
        "zSEhhRFZaQUFMaS8yQQoreFRCdFdkRW83WEdVdWpLRHZDMi9hWkt1a2Zq",
        "cE9pVUk4QWhMQWZqbWxjRC9VWjFRUGgwbUhzZ2xSTkNtcEN3Cm13U1hBO",
        "VZObWh6K1BpQitEbWw0V1duS1cvVkhvMnVqVFh4cTcrZWZNVTRIMmZueT",
        "NTZTNLWU9zRlBGR1oxVE4KUVNZbEZ1U2hXckhQdGlMbVVkUG9QNkNWMm1",
        "NTDF0aytsN0RJSXFYclFoTFVLREFDZU01cm9NeDBrTGhVV0I4UAorMHVq",
        "MUNObE5ONEpSWmxDN3hGZnFpTWJGUlU5WjRONll3SURBUUFCCi0tLS0tR",
        "U5EIFJTQSBQVUJMSUMgS0VZLS0tLS0K",
    ).joinToString("")

private val FIGURE_35 = """
   "cryptoKeys": {
     "mykey2": {
       "uri": "$EMBEDDED_KEY"
     }
   }
"""

// RFC 9553, Figure 36, with the closing brace the RFC leaves out.
private const val FIGURE_36 = """
   "directories": {
     "dir1": {
       "kind": "entry",
       "uri": "https://dir.example.com/addrbook/jdoe/Jean%20Dupont.vcf"
     },
     "dir2": {
       "kind": "directory",
       "uri": "ldap://ldap.example/o=Example%20Tech,ou=Engineering",
       "pref": 1
     }
   }
"""

// RFC 9553, Figure 37.
private const val FIGURE_37 = """
   "links": {
     "link3": {
       "kind": "contact",
       "uri": "mailto:contact@example.com",
       "pref": 1
     }
   }
"""
