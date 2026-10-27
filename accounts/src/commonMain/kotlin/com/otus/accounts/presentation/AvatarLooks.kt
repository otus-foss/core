package com.otus.accounts.presentation

import com.otus.accounts.domain.ConnectedAccount
import com.otus.avatar.AvatarMark
import com.otus.avatar.AvatarStyle
import com.otus.avatar.GeneratedAvatar
import com.otus.avatar.ResolvedAvatar
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.elements.AvatarSymbols
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.parseHexColor
import org.jetbrains.compose.resources.DrawableResource

/**
 * The design-system avatar for this result, drawing the style the user built when there is one. A
 * website icon style shows the icon, or its color with the initials until the icon loads.
 */
public fun ResolvedAvatar.toChipAvatar(): ChipAvatar {
    val style = style
    return when {
        style == null -> ChipAvatar(fallback.initials, fallback.colorIndex, image?.bytes)
        style.mark == AvatarMark.Website -> styledChip(fallback.initials, style.color, image = image?.bytes)
        else -> style.toChipAvatar()
    }
}

/** The initials over their color. */
internal fun GeneratedAvatar.toChipAvatar(): ChipAvatar = ChipAvatar(initials, colorIndex)

/** A style's color, and its letters or icon. An unknown icon name falls back to the color alone. */
public fun AvatarStyle.toChipAvatar(): ChipAvatar =
    styledChip(
        (mark as? AvatarMark.Letters)?.text.orEmpty(),
        color,
        symbol = (mark as? AvatarMark.Icon)?.let { AvatarSymbols.icons[it.name] },
    )

/** [color] is a folder color name or a custom `#RRGGBB`; anything else becomes the first folder color. */
internal fun styledChip(
    initials: String,
    color: String,
    image: ByteArray? = null,
    symbol: DrawableResource? = null,
): ChipAvatar {
    val custom = parseHexColor(color)
    return ChipAvatar(initials, 0, image, if (custom == null) presetColor(color) else null, symbol, custom)
}

/** The preset colors a style can use: every folder color except Default, which has no fill. */
internal val AvatarColors: List<FolderColor> = FolderColor.entries - FolderColor.Default

private fun presetColor(name: String): FolderColor =
    FolderColor.named(name).takeIf { it in AvatarColors } ?: AvatarColors.first()

/** The account's own mail addresses: its sign-in when that is one, and its sending addresses without wildcards. */
public val ConnectedAccount.ownAddresses: List<String>
    get() =
        (listOf(username) + emailAddresses)
            .map { it.trim() }
            .filter { '@' in it && '*' !in it }
            .distinctBy { it.lowercase() }
