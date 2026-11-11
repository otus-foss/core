package com.otusfoss.core.avatar

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AvatarBrandLogoTest {
    private val logo = AvatarImage(byteArrayOf(1))
    private val icon = AvatarImage(byteArrayOf(2))
    private val domains = PublicSuffixDomainResolver { "com" }
    private val options =
        AvatarOptions(
            domainIcons = true,
            iconProxy = IconProxy("https://mail.example.net/icon/{domain}", "https://mail.example.net/logo/{domain}"),
        )
    private val urls = mutableListOf<String>()
    private val service =
        AvatarService(
            images =
                AvatarImageLoader {
                    urls += it
                    if ("/logo/" in it) logo else icon
                },
            domains = domains,
        )

    @Test
    fun mailThatPassedDmarcShowsTheSitesBrandLogo() =
        runTest {
            val request = AvatarRequest("work", "news@mail.shop.com", brandDomain = "mail.shop.com")
            assertSame(logo, service.resolve(request, options).image)
            assertSame(logo, service.cached(request, options)?.image)
            assertEquals(listOf("https://mail.example.net/logo/shop.com"), urls)
        }

    @Test
    fun mailWithoutADmarcPassShowsTheIconEvenAfterALogoWasFound() =
        runTest {
            service.resolve(AvatarRequest("work", "news@shop.com", brandDomain = "shop.com"), options)
            val spoofed = AvatarRequest("work", "news@shop.com")
            assertSame(icon, service.resolve(spoofed, options).image)
            assertSame(icon, service.cached(spoofed, options)?.image)
        }

    @Test
    fun aDmarcPassForAnotherSiteDoesNotCount() =
        runTest {
            val request = AvatarRequest("work", "news@shop.com", brandDomain = "other.com")
            assertSame(icon, service.resolve(request, options).image)
            assertEquals(listOf("https://mail.example.net/icon/shop.com"), urls)
        }

    @Test
    fun aSiteWithoutALogoFallsBackToItsIcon() =
        runTest {
            val pixel =
                AvatarService(
                    images = AvatarImageLoader { if ("/logo/" in it) onePixelPng() else icon },
                    domains = domains,
                )
            val request = AvatarRequest("work", "news@shop.com", brandDomain = "shop.com")
            assertSame(icon, pixel.resolve(request, options).image)
        }

    @Test
    fun aOnePixelIconCountsAsNoIcon() =
        runTest {
            val pixel = AvatarService(images = AvatarImageLoader { onePixelPng() }, domains = domains)
            assertNull(pixel.resolve(AvatarRequest("work", "news@shop.com"), options).image)
        }
}

/** The signature and IHDR chunk of a 1×1 PNG, as far as the size check reads. */
private fun onePixelPng(): AvatarImage =
    AvatarImage(
        byteArrayOf(
            0x89.toByte(),
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A,
            0,
            0,
            0,
            0x0D,
            0x49,
            0x48,
            0x44,
            0x52,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            1,
        ),
    )
