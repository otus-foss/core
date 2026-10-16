package com.otus.settings.presentation

import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.CustomTheme
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.ThemeColor
import com.otus.designsystem.theme.parseHexColor
import com.otus.designsystem.theme.toHex

/**
 * Stores a choice as its built-in theme and accent, then the selected custom theme, each custom
 * theme as its number and the theme it was made from, its name with spaces escaped, and each
 * changed color, such as `light #2383E2 custom=1 1:ink 1:name=Night%20ink 1:page=#101011`.
 */
internal fun ThemeChoice.encode(): String =
    buildList {
        add(base.name.lowercase())
        add(accent.toHex())
        customId?.let { add("$SELECTED$it") }
        customThemes.forEach { theme ->
            add("${theme.id}:${theme.base.name.lowercase()}")
            theme.name?.let { add("${theme.id}:$NAME${it.escaped()}") }
            theme.colors.forEach { (color, value) -> add("${theme.id}:${color.name.lowercase()}=${value.toHex()}") }
        }
    }.joinToString(" ")

/** Reads [encode]'s format; an unknown theme or color falls back to the default for that part. */
internal fun decodeThemeChoice(value: String): ThemeChoice {
    val parts = value.trim().split(' ')
    val default = ThemeChoice()
    val rest = parts.drop(2)
    val colors =
        rest
            .mapNotNull(::decodeColor)
            .groupBy({ it.first }, { it.second })
    val names = rest.mapNotNull(::decodeName).toMap()
    val themes =
        rest.mapNotNull(::decodeTheme).map { (id, base) ->
            CustomTheme(id, base, colors[id].orEmpty().toMap(), names[id])
        }
    return ThemeChoice(
        base = themeNamed(parts.first()) ?: default.base,
        accent = parts.getOrNull(1)?.let(::parseHexColor) ?: default.accent,
        customThemes = themes,
        customId =
            rest
                .firstOrNull { it.startsWith(SELECTED) }
                ?.removePrefix(SELECTED)
                ?.toIntOrNull()
                ?.takeIf { id -> themes.any { it.id == id } },
    )
}

/** Reads a `1:ink` part: a custom theme and the theme it was made from. */
private fun decodeTheme(part: String): Pair<Int, ThemeBase>? {
    val id = part.substringBefore(':').toIntOrNull()
    val base = themeNamed(part.substringAfter(':', ""))
    return if (id == null || base == null) null else id to base
}

/** Reads a `1:page=#RRGGBB` part: one changed color of a custom theme. */
private fun decodeColor(part: String): Pair<Int, Pair<ThemeColor, Color>>? {
    val id = part.substringBefore(':').toIntOrNull()
    val name = part.substringAfter(':', "").substringBefore('=')
    val color = ThemeColor.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    val value = parseHexColor(part.substringAfter('=', ""))
    return if (id == null || color == null || value == null) null else id to (color to value)
}

/** Reads a `1:name=Night%20ink` part: the name the user gave a custom theme. */
private fun decodeName(part: String): Pair<Int, String>? {
    val id = part.substringBefore(':').toIntOrNull()
    val name =
        part
            .substringAfter(':', "")
            .takeIf { it.startsWith(NAME) }
            ?.removePrefix(NAME)
            ?.unescaped()
    return if (id == null || name.isNullOrBlank()) null else id to name
}

/** Escapes `%` and spaces, which separate the parts of [encode]'s format. */
private fun String.escaped(): String = replace("%", "%25").replace(" ", "%20")

private fun String.unescaped(): String = escapes.replace(this) { if (it.value == "%20") " " else "%" }

private val escapes = Regex("%2[05]")

private fun themeNamed(name: String): ThemeBase? =
    ThemeBase.entries.firstOrNull {
        it.name.equals(name, ignoreCase = true)
    }

private const val SELECTED = "custom="
private const val NAME = "name="
