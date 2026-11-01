package com.otus.settings.presentation

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
                section("Reading") { toggle("Show quoted text", on = true) }
                page("Action bar", { opened += "Action bar: $it" }) { setting("Layout", "Per screen") }
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
    fun aBlankQueryFindsNothing() {
        assertEquals(emptyList<SettingsItem>(), index.search("  "))
    }
}
