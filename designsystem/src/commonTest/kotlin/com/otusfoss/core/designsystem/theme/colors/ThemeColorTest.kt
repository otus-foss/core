package com.otusfoss.core.designsystem.theme.colors

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeColorTest {
    @Test
    fun eachColorReadsItsOwnPaletteSlot() {
        val palette = OtusPalette.Otus
        val expected =
            mapOf(
                ThemeColor.Page to palette.bgRegular,
                ThemeColor.Raised to palette.bgDark,
                ThemeColor.Controls to palette.bgLight,
                ThemeColor.Borders to palette.strokeBgDark,
                ThemeColor.Text to palette.text1,
                ThemeColor.SecondaryText to palette.text2,
                ThemeColor.MutedText to palette.text3,
                ThemeColor.AccentFill to palette.accentDark,
                ThemeColor.AccentOutline to palette.accentRegular,
                ThemeColor.AccentText to palette.accentLight,
            )
        assertEquals(expected, ThemeColor.entries.associateWith { palette[it] })
    }

    @Test
    fun onlyTheThreeAccentShadesCountAsAccent() {
        assertEquals(
            listOf(ThemeColor.AccentFill, ThemeColor.AccentOutline, ThemeColor.AccentText),
            ThemeColor.entries.filter { it.isAccent },
        )
    }
}
