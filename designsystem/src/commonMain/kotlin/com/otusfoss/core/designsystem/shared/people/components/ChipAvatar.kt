package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.shared.people.elements.Avatar
import com.otusfoss.core.designsystem.shared.people.elements.AvatarBadge
import com.otusfoss.core.designsystem.shared.people.elements.avatarFill
import com.otusfoss.core.designsystem.shared.people.elements.rememberAvatarImage
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.readableOn
import org.jetbrains.compose.resources.DrawableResource

/**
 * One person's avatar: generated [initials] on a [colorIndex] fill, optional encoded image bytes,
 * or a color the person picked, with [symbol] or [initials] on it. The picked color is a [color]
 * that follows the theme, or a [customColor] that stays the same in every theme.
 */
public class ChipAvatar(
    public val initials: String,
    public val colorIndex: Int,
    public val imageBytes: ByteArray? = null,
    public val color: FolderColor? = null,
    public val symbol: DrawableResource? = null,
    public val customColor: Color? = null,
)

/** Draws [avatar] at [size], decoding its image once; [modifier] can add a ring with `border`. */
@Composable
public fun PersonAvatar(
    avatar: ChipAvatar,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Avatar(
        avatar.initials,
        modifier,
        image = rememberAvatarImage(avatar.imageBytes),
        size = size,
        fill = avatar.fill,
        symbol = avatar.symbol,
        content = avatar.content,
    )
}

/**
 * Draws [avatar] as a [BadgedAvatar][com.otusfoss.core.designsystem.shared.people.elements.BadgedAvatar]'s
 * badge, such as the address a mail came to.
 */
@Composable
public fun PersonBadge(
    avatar: ChipAvatar,
    modifier: Modifier = Modifier,
) {
    AvatarBadge(
        avatar.initials,
        avatar.colorIndex,
        modifier,
        image = rememberAvatarImage(avatar.imageBytes),
        fill = avatar.fill,
        symbol = avatar.symbol,
        content = avatar.content,
    )
}

private val ChipAvatar.picked: Color?
    get() = customColor ?: color?.tint

private val ChipAvatar.fill: Color
    get() = picked ?: avatarFill(colorIndex)

/** Letters and symbols on a picked color use `text1` or `bgDark`, whichever contrasts more. */
private val ChipAvatar.content: Color
    get() = picked?.let(::readableOn) ?: OtusColors.text1
