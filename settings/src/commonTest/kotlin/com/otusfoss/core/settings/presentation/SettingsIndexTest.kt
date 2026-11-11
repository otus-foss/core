package com.otusfoss.core.settings.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsIndexTest {
    private val opened = mutableListOf<String?>()

    private val index =
        settingsIndex {
            page("Mail behavior", { opened += it }, summary = "Inbox, swipes, and reading") {
                section("Actions") {
                    setting("Swipe right", "Archive", listOf("Archive", "Trash"))
                    setting("Swipe left", "Trash", listOf("Archive", "Trash"))
                }
                section("Reading") { toggle("Show quoted text", on = true, keywords = listOf("Replies")) }
                page("Action bar", { opened += "Action bar: $it" }) { setting("Layout", "Per screen") }
                page("Filter panel", { opened += it }) {
                    keywords("Control panel")
                    unavailable("Add the Quick filters section first") { setting("Quick filters", "2 shown") }
                    unavailable(reason = null) { setting("Quick view", "3 shown") }
                }
            }
        }

    @Test
    fun matchesByNameComeBeforeOptionsAndOptionsBeforePaths() {
        val found = index.search("a")
        assertEquals(listOf("Mail behavior", "Action bar", "Layout"), found.take(3).map { it.title })
        assertEquals(listOf("Swipe right", "Swipe left"), index.search("archive").map { it.title })
        assertEquals(listOf("Swipe right", "Swipe left"), index.search("actions").map { it.title })
    }

    @Test
    fun aSettingKnowsItsPageAndSection() {
        val layout = index.search("layout").single()
        assertEquals(listOf("Mail behavior", "Action bar"), layout.page?.path)
        assertEquals(null, layout.detail)
        assertEquals("Per screen", layout.value)
        assertEquals("On", index.search("quoted").single().value)
    }

    @Test
    fun openingAResultOpensItsPageOnTheSettingAndAHeadingOpensThePageAtTheTop() {
        index.search("swipe left").single().onOpen()
        index
            .search("layout")
            .single()
            .page
            ?.onOpen()
        assertEquals(listOf<String?>("Swipe left", "Action bar: null"), opened)
    }

    @Test
    fun keywordsFindASettingAsItsOptionsDo() {
        assertEquals(listOf("Show quoted text"), index.search("replies").map { it.title })
        assertEquals(listOf("Filter panel"), index.search("control panel").map { it.title })
    }

    @Test
    fun everyWordOfAQueryCanMatchADifferentField() {
        // "swipe" is in the title and "trash" in an option; no field holds the whole query.
        assertEquals(listOf("Swipe right", "Swipe left"), index.search("swipe trash").map { it.title })
        assertEquals(emptyList<SettingsItem>(), index.search("swipe sender"))
    }

    @Test
    fun anUnavailableSettingIsFoundWithItsReason() {
        val quick = index.search("quick filters").single()
        assertEquals("Add the Quick filters section first", quick.unavailable)
        assertEquals(listOf("Mail behavior", "Filter panel"), quick.page?.path)
        assertEquals(null, index.search("layout").single().unavailable)
        assertEquals(null, index.search("quick view").single().unavailable)
    }

    @Test
    fun aBlankQueryFindsNothing() {
        assertEquals(emptyList<SettingsItem>(), index.search("  "))
    }
}
