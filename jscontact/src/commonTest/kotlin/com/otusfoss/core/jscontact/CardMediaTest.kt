package com.otusfoss.core.jscontact

import com.otusfoss.core.jscontact.RoundTrip.assertEntriesRoundTrip
import com.otusfoss.core.jscontact.RoundTrip.decodeEntries
import com.otusfoss.core.jscontact.RoundTrip.encodeEntries
import com.otusfoss.core.jscontact.RoundTrip.fragment
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CardMediaTest {
    @Test
    fun mediaOfEveryKindRoundTrip() {
        val (media, _) = assertEntriesRoundTrip(Media.serializer(), FIGURE_38, "media")
        assertEquals(
            Media(MediaKind.Sound, "CID:JOHNQ.part8.19960229T080000.xyzMail@example.com"),
            media.getValue("res45"),
        )
        assertEquals(MediaKind.Logo, media.getValue("res47").kind)
        val photo = media.getValue("res1")
        assertEquals(MediaKind.Photo, photo.kind)
        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAASABIAAD/4...", photo.uri)
        assertNull(photo.blobId)
    }

    @Test
    fun aServerMayReturnABlobIdInPlaceOfTheUri() {
        val text = """"media": {"p": {"kind": "photo", "blobId": "Gd2f", "mediaType": "image/jpeg", "pref": 1}}"""
        val photo = assertEntriesRoundTrip(Media.serializer(), text, "media").first.getValue("p")
        assertEquals("", photo.uri)
        assertEquals("Gd2f", photo.blobId)
        assertEquals("image/jpeg", photo.mediaType)
    }

    @Test
    fun aNewPhotoSendsItsBlobIdWithoutAUri() {
        val photo = Media(MediaKind.Photo, blobId = "Gb1", mediaType = "image/png")
        assertEquals(
            JsContactJson.parseToJsonElement(
                """{"@type": "Media", "kind": "photo", "blobId": "Gb1", "mediaType": "image/png"}""",
            ),
            encodeEntries(Media.serializer(), mapOf("m" to photo)).getValue("m"),
        )
    }

    @Test
    fun mediaKeepsVendorKindsAndProperties() {
        val text = """"media": {"v": {"kind": "example.com:video", "uri": "https://example.com/v.mp4",
            "contexts": {"work": true}, "label": "Intro", "example.com:length": 42}}"""
        val video = assertEntriesRoundTrip(Media.serializer(), text, "media").first.getValue("v")
        assertEquals(MediaKind("example.com:video"), video.kind)
        assertEquals(mapOf(ContactContext.Work to true), video.contexts)
        assertEquals("Intro", video.label)
        assertEquals(setOf("example.com:length"), video.extra.keys)
    }

    @Test
    fun mediaWithoutKindIsRejected() {
        val text = """"media": {"p": {"uri": "https://example.com/p.jpg"}}"""
        assertFailsWith<SerializationException> { decodeEntries(Media.serializer(), fragment(text, "media")) }
    }
}

// RFC 9553, Figure 38. The RFC cuts the photo data short with "...", which the round trip keeps as text.
private const val FIGURE_38 = """
   "media": {
     "res45": {
       "kind": "sound",
       "uri": "CID:JOHNQ.part8.19960229T080000.xyzMail@example.com"
     },
     "res47": {
       "kind": "logo",
       "uri": "https://www.example.com/pub/logos/abccorp.jpg"
     },
     "res1": {
       "kind": "photo",
       "uri": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAASABIAAD/4..."
     }
   }
"""
