package com.otus.avatar

import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

// Every other avatar test passes its own rules, so only this one reads the list the module ships.
// Compose resources read it through an Android context, which Robolectric provides.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BundledSuffixListTest {
    @Test
    fun theDefaultResolverReadsTheBundledList() =
        runTest {
            assertEquals("example.co.uk", PublicSuffixDomainResolver().resolve("mail.example.co.uk"))
            assertEquals("me.github.io", PublicSuffixDomainResolver().resolve("www.me.github.io"))
        }
}
