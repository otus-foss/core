package com.otus.avatar

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpWebsiteIconSourceTest {
    @Test
    fun redirectedWebsiteUsesItsLargeAdvertisedIconInsteadOfTheSmallFavicon() =
        runTest {
            val urls = mutableListOf<String>()
            val bytes = byteArrayOf(1, 2, 3)
            val loader =
                HttpAvatarImageLoader(
                    MockEngine(
                        MockEngineConfig().apply {
                            dispatcher = StandardTestDispatcher(testScheduler)
                            addHandler { request ->
                                urls += request.url.toString()
                                when (request.url.host) {
                                    "pm.me" -> {
                                        respond(
                                            "",
                                            HttpStatusCode.Found,
                                            headersOf(HttpHeaders.Location, "https://proton.me/mail"),
                                        )
                                    }

                                    else -> {
                                        if (request.url.encodedPath == "/mail") {
                                            val html =
                                                """
                                                <head>
                                                <link rel="icon" sizes="32x32" href="/favicons/small.png">
                                                <link rel="apple-touch-icon" sizes="180x180" href="/favicons/apple-touch-icon.png">
                                                </head>
                                                """.trimIndent() + "x".repeat(100_000)
                                            respond(html, headers = headersOf(HttpHeaders.ContentType, "text/html"))
                                        } else {
                                            respond(bytes, headers = headersOf(HttpHeaders.ContentType, "image/png"))
                                        }
                                    }
                                }
                            }
                        },
                    ),
                )
            try {
                assertContentEquals(bytes, HttpWebsiteIconSource(loader).icon("pm.me")?.bytes)
                assertEquals(
                    listOf(
                        "https://pm.me/",
                        "https://proton.me/mail",
                        "https://proton.me/favicons/apple-touch-icon.png",
                    ),
                    urls,
                )
            } finally {
                loader.close()
            }
        }

    @Test
    fun websiteRedirectIntoTheLocalNetworkIsRefusedBeforeFetching() =
        runTest {
            val urls = mutableListOf<String>()
            val loader =
                HttpAvatarImageLoader(
                    MockEngine(
                        MockEngineConfig().apply {
                            dispatcher = StandardTestDispatcher(testScheduler)
                            addHandler { request ->
                                urls += request.url.toString()
                                respond(
                                    "",
                                    HttpStatusCode.Found,
                                    headersOf(HttpHeaders.Location, "https://printer.lan/admin"),
                                )
                            }
                        },
                    ),
                )
            try {
                assertNull(HttpWebsiteIconSource(loader).icon("attacker.com"))
                assertEquals(listOf("https://attacker.com/"), urls)
            } finally {
                loader.close()
            }
        }

    @Test
    fun linkParsingHandlesQuotesRelativeUrlsAndSkipsVectorIcons() {
        val page =
            AvatarWebsitePage(
                "https://company.com/mail/",
                """
                <LINK REL='icon' TYPE='image/svg+xml' HREF='/vector.svg' sizes='any'>
                <link rel='mask-icon' href='/mask.svg'>
                <link rel=icon sizes=32x32 href=small.png>
                <link rel='apple-touch-icon' href='https://cdn.example.net/large.png?a=1&amp;b=2'>
                <link rel='' href=''>
                """.trimIndent(),
            )
        assertEquals(
            listOf("https://cdn.example.net/large.png?a=1&b=2", "https://company.com/mail/small.png"),
            websiteIconUrls(page),
        )
    }

    @Test
    fun advertisedUnsafeUrlsAreRefusedAndNextSafeIconIsUsed() =
        runTest {
            val paths = mutableListOf<String>()
            val bytes = byteArrayOf(1)
            val loader =
                HttpAvatarImageLoader(
                    MockEngine(
                        MockEngineConfig().apply {
                            dispatcher = StandardTestDispatcher(testScheduler)
                            addHandler { request ->
                                paths += request.url.encodedPath
                                if (request.url.encodedPath == "/") {
                                    respond(
                                        """
                                        <link rel=apple-touch-icon href='https://user:pass@other.com/secret.png'>
                                        <link rel=apple-touch-icon href='http://other.com/insecure.png'>
                                        <link rel=apple-touch-icon href='https://printer.lan/admin.png'>
                                        <link rel=apple-touch-icon href='//10.0.0.1/admin.png'>
                                        <link rel=icon href='/safe.png'>
                                        """.trimIndent(),
                                        headers = headersOf(HttpHeaders.ContentType, "text/html"),
                                    )
                                } else {
                                    respond(bytes, headers = headersOf(HttpHeaders.ContentType, "image/png"))
                                }
                            }
                        },
                    ),
                )
            try {
                assertContentEquals(bytes, HttpWebsiteIconSource(loader).icon("company.com")?.bytes)
                assertEquals(listOf("/", "/safe.png"), paths)
            } finally {
                loader.close()
            }
        }
}
