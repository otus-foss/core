package com.otus.avatar

import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JmapOwnAvatarsTest {
    private val photo = ContactAvatar.Photo(AvatarImage(byteArrayOf(1, 2, 3)), "image/png")
    private val letters = ContactAvatar.Styled(AvatarStyle("blue", AvatarMark.Letters("AL")))

    @Test
    fun aPhotoOnACardWithoutMediaSetsTheWholeMap() =
        runTest {
            val server = SelfCardServer(card = """{"id":"c","addressBookIds":{"own":true},$ADA_EMAIL}""")
            server.avatars.save("ada", "ada@example.org", "Ada", photo)
            val patch = server.update()
            assertEquals(setOf("media"), patch.keys)
            val media =
                patch
                    .getValue("media")
                    .jsonObject
                    .getValue(AVATAR_PHOTO_KEY)
                    .jsonObject
            assertEquals(JsonPrimitive("data:image/png;base64,AQID"), media["uri"])
        }

    @Test
    fun aPhotoReplacesTheOldPhotosButKeepsOtherMedia() =
        runTest {
            val media = """"media":{"p":{"kind":"photo","uri":"data:,"},"l":{"kind":"logo","uri":"data:,"}}"""
            val server = SelfCardServer(card = """{"id":"c","addressBookIds":{"own":true},$ADA_EMAIL,$media}""")
            server.avatars.save("ada", "ada@example.org", "Ada", photo)
            val patch = server.update()
            assertEquals(setOf("media/p", "media/$AVATAR_PHOTO_KEY"), patch.keys)
            assertEquals("null", patch.getValue("media/p").toString())
        }

    @Test
    fun aStyleOnACardWithoutVCardSetsTheWholeObject() =
        runTest {
            val server = SelfCardServer(card = """{"id":"c","addressBookIds":{"own":true},$ADA_EMAIL}""")
            server.avatars.save("ada", "ada@example.org", "Ada", letters)
            val vCard = server.update().getValue("vCard").jsonObject
            assertEquals(letters.style, avatarStyleIn(vCard))
        }

    @Test
    fun resettingRemovesTheStyleAndPhotos() =
        runTest {
            val style = avatarPropertiesWith(null, letters.style)
            val card =
                """{"id":"c","addressBookIds":{"own":true},$ADA_EMAIL,
                "media":{"$AVATAR_PHOTO_KEY":{"kind":"photo","uri":"data:,"}},"vCard":{"properties":$style}}"""
            val server = SelfCardServer(card = card)
            server.avatars.save("ada", "ada@example.org", "Ada", null)
            val patch = server.update()
            assertEquals("null", patch.getValue("media/$AVATAR_PHOTO_KEY").toString())
            assertTrue(patch.getValue("vCard/properties").jsonArray.isEmpty())
        }

    @Test
    fun aNewSelfCardGoesInTheDefaultWritableBook() =
        runTest {
            val server = SelfCardServer(card = null)
            server.avatars.save("ada", "Ada@Example.org", "Ada", letters)
            val created =
                server
                    .set()
                    .getValue("create")
                    .jsonObject.values
                    .single()
                    .jsonObject
            assertEquals("""{"own":true}""", created.getValue("addressBookIds").toString())
            assertTrue("ada@example.org" in created.getValue("emails").toString())
            assertEquals(letters.style, avatarStyleIn(created["vCard"]))
        }

    @Test
    fun aSavedAvatarShowsAtOnceThoughTheSearchDoesNotFindTheNewCardYet() =
        runTest {
            val server = SelfCardServer(card = null)
            server.avatars.save("ada", "ada@example.org", "Ada", letters)
            assertEquals(letters.style, server.avatars.avatar("ada", "ada@example.org", "Ada").style)
            server.avatars.save("ada", "ada@example.org", "Ada", photo)
            val shown = server.avatars.avatar("ada", "ada@example.org", "Ada")
            assertEquals(AvatarOrigin.Contact, shown.origin)
            assertTrue(shown.image?.bytes.contentEquals(photo.image.bytes))
        }

    @Test
    fun aSecondSaveUpdatesTheCardTheFirstMadeInsteadOfMakingAnother() =
        runTest {
            val server = SelfCardServer(card = null)
            server.avatars.save("ada", "ada@example.org", "Ada", letters)
            server.avatars.save("ada", "ada@example.org", "Ada", photo)
            assertTrue("create" in server.sets[0])
            val second = server.sets[1]
            assertNull(second["create"], "No second self card: $second")
            assertEquals(setOf("n"), second.getValue("update").jsonObject.keys)
        }

    @Test
    fun resettingWithoutASelfCardWritesNothing() =
        runTest {
            val server = SelfCardServer(card = null)
            server.avatars.save("ada", "ada@example.org", "Ada", null)
            assertNull(server.sets.firstOrNull())
        }

    @Test
    fun aCardInASharedBookIsNotTheSelfCard() =
        runTest {
            val server = SelfCardServer(card = """{"id":"c","addressBookIds":{"shared":true},$ADA_EMAIL}""")
            server.avatars.save("ada", "ada@example.org", "Ada", letters)
            assertTrue("create" in server.set())
        }

    @Test
    fun withoutAWritableBookTheSaveIsReadOnly() =
        runTest {
            val server = SelfCardServer(card = null, books = SHARED_BOOK)
            val error =
                assertFailsWith<OwnAvatarException> { server.avatars.save("ada", "ada@example.org", "", letters) }
            assertEquals(OwnAvatarFailure.ReadOnly, error.reason)
        }

    @Test
    fun aForbiddenUpdateIsReadOnly() =
        runTest {
            val server =
                SelfCardServer(
                    card = """{"id":"c","addressBookIds":{"own":true},$ADA_EMAIL}""",
                    setResult = """{"newState":"2","notUpdated":{"c":{"type":"forbidden"}}}""",
                )
            val error =
                assertFailsWith<OwnAvatarException> { server.avatars.save("ada", "ada@example.org", "", letters) }
            assertEquals(OwnAvatarFailure.ReadOnly, error.reason)
        }
}

/**
 * A contacts server with [books] and at most one [card], which records each ContactCard/set. Like a
 * server that indexes in the background, its search never finds the card a set made, which only a
 * get by its id returns.
 */
private class SelfCardServer(
    private val card: String?,
    private val books: String = "$OWN_BOOK,$SHARED_BOOK",
    /** A fixed answer to every set; by default each set is answered with exactly what it asked. */
    private val setResult: String? = null,
) {
    val sets = mutableListOf<JsonObject>()
    private var made: String? = null
    private val engine =
        MockEngine.create {
            dispatcher = Dispatchers.Unconfined
            addHandler { request ->
                val json = headersOf(HttpHeaders.ContentType, "application/json")
                if (request.url.encodedPath == "/jmap/session") return@addHandler respond(SESSION, headers = json)
                val responses =
                    Json
                        .parseToJsonElement((request.body as TextContent).text)
                        .jsonObject
                        .getValue("methodCalls")
                        .jsonArray
                        .joinToString(",") { call ->
                            val (name, arguments, id) = call.jsonArray
                            val result = result((name as JsonPrimitive).content, arguments.jsonObject)
                            """[$name,$result,$id]"""
                        }
                respond("""{"methodResponses":[$responses]}""", headers = json)
            }
        }
    private val logins = adaLogins()
    val avatars =
        JmapOwnAvatars(JmapConnections(JmapClient(engine), logins), logins, AvatarService()) { AvatarOptions() }

    private fun result(
        method: String,
        arguments: JsonObject,
    ): String =
        when (method) {
            "AddressBook/get" -> """{"list":[$books]}"""
            "ContactCard/query" -> """{"ids":[${if (card == null) "" else "\"c\""}]}"""
            "ContactCard/get" -> getResult(arguments)
            "ContactCard/set" -> setResult(arguments)
            else -> error("Unexpected $method")
        }

    private fun getResult(arguments: JsonObject): String {
        val byId = arguments["ids"] is JsonArray
        return """{"list":[${(if (byId) made else card).orEmpty()}]}"""
    }

    private fun setResult(arguments: JsonObject): String {
        sets += arguments
        if ("create" in arguments) made = MADE_CARD
        return setResult ?: accepted(arguments)
    }

    fun set(): JsonObject = sets.single()

    fun update(): JsonObject =
        set()
            .getValue("update")
            .jsonObject
            .getValue("c")
            .jsonObject
}

private const val ADA_EMAIL = """"emails":{"e":{"address":"ada@example.org"}}"""
private const val MADE_CARD = """{"id":"n","addressBookIds":{"own":true},$ADA_EMAIL}"""
private const val OWN_BOOK = """{"id":"own","isDefault":true,"myRights":{"mayRead":true,"mayWrite":true}}"""
private const val SHARED_BOOK = """{"id":"shared","myRights":{"mayRead":true,"mayWrite":false}}"""
private const val SESSION = """
{"capabilities":{"urn:ietf:params:jmap:core":{},"urn:ietf:params:jmap:contacts":{}},
 "accounts":{"contacts":{"accountCapabilities":{"urn:ietf:params:jmap:contacts":{}}}},
 "primaryAccounts":{"urn:ietf:params:jmap:contacts":"contacts"},"apiUrl":"/jmap/"}
"""

/** A set answer that creates and updates exactly what [arguments] asked, as a server must (RFC 8620, section 5.3). */
private fun accepted(arguments: JsonObject): String {
    val created =
        arguments["create"]
            ?.jsonObject
            ?.keys
            .orEmpty()
            .joinToString(",") { """"$it":{"id":"n"}""" }
    val updated =
        arguments["update"]
            ?.jsonObject
            ?.keys
            .orEmpty()
            .joinToString(",") { """"$it":null""" }
    return """{"newState":"2","created":{$created},"updated":{$updated}}"""
}
