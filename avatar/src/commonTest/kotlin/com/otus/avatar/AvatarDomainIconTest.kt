package com.otus.avatar

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class AvatarDomainIconTest {
    private val request = AvatarRequest("work", "noreply@email.openai.com", "OpenAI")
    private val image = AvatarImage(byteArrayOf(1, 2, 3))
    private val options = AvatarOptions(domainIcons = true)
    private val proxied = options.copy(iconProxy = IconProxy("https://icons.example.net/{domain}.ico"))
    private val domains = PublicSuffixDomainResolver { "com\nme\nio\ngithub.io" }

    @Test
    fun resolverFailuresRetainInitialsWithoutFetchingIcons() =
        runTest {
            for (failure in listOf(IllegalStateException("Offline"), AssertionError("Resolver failed"))) {
                val resolvedHosts = mutableListOf<String>()
                val fetched = mutableListOf<String>()
                val service =
                    AvatarService(
                        images =
                            AvatarImageLoader {
                                fetched += it
                                image
                            },
                        domains =
                            AvatarDomainResolver {
                                resolvedHosts += it
                                yield()
                                throw failure
                            },
                    )
                val expected = ResolvedAvatar(generatedAvatar(request.name, request.email))
                assertEquals(expected, service.resolve(request, options))
                assertEquals(expected, service.cached(request, options))
                assertEquals(listOf("email.openai.com", "email.openai.com"), resolvedHosts)
                assertEquals(emptyList(), fetched)
            }
        }

    @Test
    fun resolverCancellationPropagatesFromResolveAndCached() =
        runTest {
            val cancellation = CancellationException("Refresh cancelled")
            val service =
                AvatarService(
                    domains =
                        AvatarDomainResolver {
                            yield()
                            throw cancellation
                        },
                )
            assertSame(cancellation, assertFailsWith<CancellationException> { service.resolve(request, options) })
            assertSame(cancellation, assertFailsWith<CancellationException> { service.cached(request, options) })
        }

    @Test
    fun subdomainSenderFetchesOnlyTheRegistrableDomain() =
        runTest {
            val urls = mutableListOf<String>()
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            urls += it
                            null
                        },
                    domains = domains,
                )
            val result = service.resolve(request, options)
            assertEquals(
                listOf(
                    "https://openai.com/apple-touch-icon.png",
                    "https://openai.com/favicon.png",
                    "https://openai.com/favicon.ico",
                ),
                urls,
            )
            assertNull(result.image)
        }

    @Test
    fun perRecipientSubdomainsShareOneFetchAndNeverReachTheSubdomain() =
        runTest {
            val urls = mutableListOf<String>()
            val websites = mutableListOf<String>()
            val service =
                AvatarService(
                    images =
                        object : AvatarImageLoader {
                            override suspend fun load(url: String): AvatarImage? {
                                urls += url
                                return null
                            }

                            override suspend fun websiteIcon(domain: String): AvatarImage? {
                                websites += domain
                                return image
                            }
                        },
                    domains = domains,
                )
            val first = service.resolve(request.copy(email = "x@u-12345.evil.com"), options)
            val second = service.resolve(request.copy(email = "x@u-67890.evil.com"), options)
            assertSame(image, first.image)
            assertSame(image, second.image)
            assertEquals(listOf("evil.com"), websites)
            assertEquals(emptyList(), urls)
        }

    @Test
    fun theProxyIsAskedInsteadOfTheWebsite() =
        runTest {
            val urls = mutableListOf<String>()
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            urls += it
                            image
                        },
                    domains = domains,
                )
            assertSame(image, service.resolve(request, proxied).image)
            assertEquals(listOf("https://icons.example.net/openai.com.ico"), urls)
        }

    @Test
    fun aProxyMissNeverFallsBackToTheWebsite() =
        runTest {
            val urls = mutableListOf<String>()
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            urls += it
                            null
                        },
                    domains = domains,
                )
            assertNull(service.resolve(request, proxied).image)
            assertEquals(listOf("https://icons.example.net/openai.com.ico"), urls)
        }

    @Test
    fun protonWebsiteIconIsUsedWithoutAnIconCacheRequest() =
        runTest {
            val urls = mutableListOf<String>()
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            urls += it
                            if (it.endsWith("favicon.ico")) image else null
                        },
                    domains = domains,
                )
            val result = service.resolve(request.copy(email = "ada@proton.me"), options)
            assertSame(image, result.image)
            assertEquals("https://proton.me/favicon.ico", urls.last())
            assertEquals(3, urls.size)
        }

    @Test
    fun aWebsiteStyledContactWithAnUnusableProxyAsksNothing() =
        runTest {
            val urls = mutableListOf<String>()
            val website = ContactAvatar.Styled(AvatarStyle("blue", AvatarMark.Website))
            val service =
                AvatarService(
                    contacts = ContactAvatarSource { _, _ -> website },
                    images =
                        AvatarImageLoader {
                            urls += it
                            image
                        },
                    domains = domains,
                )
            val unusable = AvatarOptions(iconProxy = IconProxy(iconUrl = null))
            assertNull(service.resolve(request, unusable).image)
            assertEquals(emptyList(), urls)
        }

    @Test
    fun theProxyIsAskedOnlyWithDomainIcons() =
        runTest {
            var calls = 0
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            calls++
                            image
                        },
                    domains = domains,
                )
            assertNull(service.resolve(request, proxied.copy(domainIcons = false)).image)
            assertEquals(0, calls)
            assertSame(image, service.resolve(request, proxied).image)
            assertEquals(1, calls)
        }

    @Test
    fun contactPhotoWinsBeforeDomainResolutionOrAnIconCacheRequest() =
        runTest {
            var calls = 0
            val service =
                AvatarService(
                    contacts = photoSource { _, _ -> image },
                    domains =
                        AvatarDomainResolver {
                            calls++
                            it
                        },
                )
            val result = service.resolve(request, proxied)
            assertSame(image, result.image)
            assertEquals(AvatarOrigin.Contact, result.origin)
            assertEquals(0, calls)
        }

    @Test
    fun publicSuffixesAndUnrelatedResolvedDomainsAreNotFetched() =
        runTest {
            var calls = 0
            val service =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            calls++
                            image
                        },
                    domains = domains,
                )
            assertNull(service.resolve(request.copy(email = "ada@github.io"), options).image)
            val unrelated =
                AvatarService(
                    images =
                        AvatarImageLoader {
                            calls++
                            image
                        },
                    domains = AvatarDomainResolver { "other.com" },
                )
            assertNull(unrelated.resolve(request, options).image)
            assertEquals(0, calls)
        }
}
