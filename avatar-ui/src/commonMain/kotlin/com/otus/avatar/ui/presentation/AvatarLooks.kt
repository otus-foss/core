package com.otus.avatar.ui.presentation

import com.otus.avatar.AvatarColor
import com.otus.avatar.AvatarMark
import com.otus.avatar.AvatarStyle
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.generatedAvatar
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.elements.AvatarSymbols
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.parseHexColor
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

/** This avatar, or the generated one of [name] and [email] while there is none. */
public fun ResolvedAvatar?.toChipAvatarOr(
    name: String,
    email: String,
): ChipAvatar = (this ?: ResolvedAvatar(generatedAvatar(name, email))).toChipAvatar()

/** A style's color, and its letters or icon. An unknown icon name falls back to the color alone. */
public fun AvatarStyle.toChipAvatar(): ChipAvatar =
    styledChip(
        (mark as? AvatarMark.Letters)?.text.orEmpty(),
        color,
        symbol = (mark as? AvatarMark.Icon)?.let { AvatarSymbols.icons[it.name] },
    )

/** [color] is an [AvatarColor] id or a custom `#RRGGBB`; anything else becomes the first preset. */
internal fun styledChip(
    initials: String,
    color: String,
    image: ByteArray? = null,
    symbol: DrawableResource? = null,
): ChipAvatar {
    val custom = parseHexColor(color)
    return ChipAvatar(initials, 0, image, if (custom == null) presetColor(color) else null, symbol, custom)
}

private fun presetColor(name: String): FolderColor = (AvatarColor.of(name) ?: AvatarColor.entries.first()).folderColor

/** The design-system color that draws this preset. */
internal val AvatarColor.folderColor: FolderColor
    get() =
        when (this) {
            AvatarColor.Red -> FolderColor.Red
            AvatarColor.Orange -> FolderColor.Orange
            AvatarColor.Yellow -> FolderColor.Yellow
            AvatarColor.Green -> FolderColor.Green
            AvatarColor.Teal -> FolderColor.Teal
            AvatarColor.Blue -> FolderColor.Blue
            AvatarColor.Purple -> FolderColor.Purple
            AvatarColor.Pink -> FolderColor.Pink
        }
