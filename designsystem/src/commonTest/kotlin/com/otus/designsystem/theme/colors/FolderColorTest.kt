package com.otus.designsystem.theme.colors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FolderColorTest {
    @Test
    fun everyColorStandsOutOnEveryThemeAndAccent() {
        ThemeBase.entries
            .flatMap { base ->
                AccentPreset.entries.map { accent ->
                    "${base.name} with ${accent.name}" to ThemeChoice(base, accent.color).palette
                }
            }.flatMap { (theme, palette) -> contrastCases(theme, palette) }
            .forEach { (case, ratio) -> assertTrue(ratio >= MIN_CONTRAST, "$case: $ratio") }
    }

    @Test
    fun defaultKeepsTheIconColor() {
        ThemeBase.entries.forEach { assertNull(FolderColor.Default.tintIn(ThemeChoice(it).palette)) }
    }

    @Test
    fun aStoredNameReadsBackAsItsColor() {
        FolderColor.entries.forEach { assertEquals(it, FolderColor.named(it.name)) }
    }

    @Test
    fun aMissingOrUnknownNameReadsAsDefault() {
        assertEquals(FolderColor.Default, FolderColor.named(null))
        assertEquals(FolderColor.Default, FolderColor.named("Mauve"))
        assertEquals(FolderColor.Default, FolderColor.named("red"))
    }

    @Test
    fun aCustomThemeUsesTheShadesOfItsKind() {
        val custom = ThemeChoice(ThemeBase.Light).withColor(ThemeColor.Page, ThemeBase.Light.page.copy(red = 0.9f))
        assertEquals(
            FolderColor.Red.tintIn(ThemeChoice(ThemeBase.Light).palette),
            FolderColor.Red.tintIn(custom.palette),
        )
    }

    private companion object {
        const val MIN_CONTRAST = 3f
    }
}

private fun contrastCases(
    theme: String,
    palette: OtusPalette,
): List<Pair<String, Float>> =
    (FolderColor.entries - FolderColor.Default).flatMap { color ->
        val tint = assertNotNull(color.tintIn(palette), "${color.name} has a tint")
        listOf(
            "bgDark" to palette.bgDark,
            "bgRegular" to palette.bgRegular,
            "accentDark" to palette.accentDark,
            "selectedFill" to palette.selectedFill,
        ).map { (surface, background) ->
            "${color.name} on $surface of $theme" to contrast(tint, background)
        }
    }
