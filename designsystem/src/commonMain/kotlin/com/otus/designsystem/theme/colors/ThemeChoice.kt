package com.otus.designsystem.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The selected theme, the one accent color the user picked, and the user's [customThemes].
 * [customId] selects a custom theme; while it is null, the built-in [base] is selected. Changing a
 * color never changes a built-in theme: it creates a custom theme instead. [palette] derives every
 * color from these.
 */
@Immutable
public data class ThemeChoice(
    public val base: ThemeBase = ThemeBase.Otus,
    public val accent: Color = AccentPreset.OtusBlue.color,
    public val customThemes: List<CustomTheme> = emptyList(),
    public val customId: Int? = null,
) {
    /** The selected custom theme, or null while a built-in theme is selected. */
    public val customTheme: CustomTheme?
        get() = customThemes.firstOrNull { it.id == customId }

    /** The built-in theme the colors start from: [base], or the one [customTheme] was made from. */
    public val origin: ThemeBase
        get() = customTheme?.base ?: base

    /** The colors the selected custom theme changes; empty for a built-in theme. */
    public val customColors: Map<ThemeColor, Color>
        get() = customTheme?.colors.orEmpty()

    /** The accent's fill, outline, and text shades for the theme, after the user's own shades. */
    public val shades: AccentShades
        get() {
            val derived = accentShades(accent, color(ThemeColor.Page), color(ThemeColor.Raised), color(ThemeColor.Text))
            return derived.copy(
                fill = customColors[ThemeColor.AccentFill] ?: derived.fill,
                regular = customColors[ThemeColor.AccentOutline] ?: derived.regular,
                text = customColors[ThemeColor.AccentText] ?: derived.text,
            )
        }

    public val palette: OtusPalette
        get() {
            val accentShades = shades
            return OtusPalette(
                accentLight = accentShades.text,
                accentRegular = accentShades.regular,
                accentDark = accentShades.fill,
                bgLight = color(ThemeColor.Controls),
                bgRegular = color(ThemeColor.Page),
                bgDark = color(ThemeColor.Raised),
                text1 = color(ThemeColor.Text),
                text2 = color(ThemeColor.SecondaryText),
                text3 = color(ThemeColor.MutedText),
                strokeBgDark = color(ThemeColor.Borders),
                isLight = color(ThemeColor.Page).isLightPage,
            )
        }

    /** Selects the built-in [base] theme. */
    public fun withBase(base: ThemeBase): ThemeChoice = copy(base = base, customId = null)

    /** Selects the custom theme [id]. */
    public fun withCustomTheme(id: Int): ThemeChoice = copy(customId = id)

    /** The accent for every theme; it replaces the accent shades set by hand in the selected theme. */
    public fun withAccent(accent: Color): ThemeChoice =
        (customTheme?.let { withTheme(it.copy(colors = it.colors.filterKeys { color -> !color.isAccent })) } ?: this)
            .copy(accent = accent)

    /**
     * Sets [color] to [value] in the selected custom theme; null, or the color the theme would
     * have anyway, puts back that color. On a built-in theme, a new color creates and selects a
     * custom theme made from it, so the built-in theme never changes.
     */
    public fun withColor(
        color: ThemeColor,
        value: Color?,
    ): ThemeChoice {
        val theme = customTheme ?: CustomTheme(nextId, base)
        val reset = theme.copy(colors = theme.colors - color)
        val unchanged = value == null || withTheme(reset).palette[color] == value
        return when {
            unchanged && customTheme == null -> this
            unchanged -> withTheme(reset)
            else -> withTheme(theme.copy(colors = theme.colors + (color to checkNotNull(value))))
        }
    }

    /** Names the custom theme [id] [name]; a blank name puts back the name the app gives it. */
    public fun withName(
        id: Int,
        name: String,
    ): ThemeChoice =
        copy(customThemes = customThemes.map { if (it.id == id) it.copy(name = name.trim().ifBlank { null }) else it })

    /** Copies the selected theme, built-in or custom, into a new custom theme called [name] and selects it. */
    public fun withCopy(name: String): ThemeChoice = withTheme(CustomTheme(nextId, origin, customColors, name))

    /** Deletes the custom theme [id]; if it was selected, the theme it was made from is selected. */
    public fun withoutCustomTheme(id: Int): ThemeChoice {
        val deleted = customThemes.firstOrNull { it.id == id } ?: return this
        return copy(
            base = if (customId == id) deleted.base else base,
            customThemes = customThemes - deleted,
            customId = customId.takeIf { it != id },
        )
    }

    /** Adds or replaces [theme] and selects it. */
    private fun withTheme(theme: CustomTheme): ThemeChoice =
        copy(
            customThemes =
                if (customThemes.any { it.id == theme.id }) {
                    customThemes.map { if (it.id == theme.id) theme else it }
                } else {
                    customThemes + theme
                },
            customId = theme.id,
        )

    private val nextId: Int
        get() = (customThemes.maxOfOrNull { it.id } ?: 0) + 1

    /** A surface or text color of the theme; only called for colors the theme defines itself. */
    private fun color(color: ThemeColor): Color = customColors[color] ?: checkNotNull(origin.color(color))
}
