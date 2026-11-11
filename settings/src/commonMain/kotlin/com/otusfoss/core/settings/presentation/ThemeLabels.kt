package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.colors.CustomTheme
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.designsystem.theme.colors.ThemeColor

/** The selected theme's name in settings, such as on the theme row. */
public val ThemeChoice.themeTitle: String
    get() = customTheme?.title ?: base.title

/** A custom theme's name: the user's, else "Custom" for the first and a number for later ones. */
internal val CustomTheme.title: String
    get() = name ?: if (id == 1) "Custom" else "Custom $id"

internal val ThemeBase.title: String
    get() = name

/** A theme color's name, such as on its row under Colors. */
public val ThemeColor.title: String
    get() =
        when (this) {
            ThemeColor.Page -> "Page"
            ThemeColor.Raised -> "Raised surfaces"
            ThemeColor.Controls -> "Controls"
            ThemeColor.Borders -> "Borders"
            ThemeColor.Text -> "Text"
            ThemeColor.SecondaryText -> "Secondary text"
            ThemeColor.MutedText -> "Muted text"
            ThemeColor.AccentFill -> "Accent fill"
            ThemeColor.AccentOutline -> "Accent outline"
            ThemeColor.AccentText -> "Accent text"
        }

/** Where the color shows, as the description under its row. */
internal val ThemeColor.description: String
    get() =
        when (this) {
            ThemeColor.Page -> "Behind every screen"
            ThemeColor.Raised -> "Sheets, cards and the sidebar"
            ThemeColor.Controls -> "Chips, buttons and outlines"
            ThemeColor.Borders -> "Lines around cards and panels"
            ThemeColor.Text -> "Titles and body text"
            ThemeColor.SecondaryText -> "Previews and labels"
            ThemeColor.MutedText -> "Dates and hints"
            ThemeColor.AccentFill -> "Selected items and primary buttons"
            ThemeColor.AccentOutline -> "Outlines of primary buttons"
            ThemeColor.AccentText -> "Links, rings and accent text"
        }
