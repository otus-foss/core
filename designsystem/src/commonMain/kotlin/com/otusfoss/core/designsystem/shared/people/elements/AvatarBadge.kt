package com.otusfoss.core.designsystem.shared.people.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.people.AvatarTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * [avatar] with [badge] on its bottom-right edge. The badge sits outside the avatar's clip, so it
 * can overhang it.
 */
@Composable
public fun BadgedAvatar(
    badge: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit,
) {
    Box(modifier) {
        avatar()
        if (badge != null) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(-AvatarTokens.badgeOffset, -AvatarTokens.badgeOffset),
            ) { badge() }
        }
    }
}

/**
 * A small avatar for a [BadgedAvatar]'s badge, such as the account a mail arrived in. A `bgRegular` ring
 * cuts it out of the avatar under it. It is decoration: the row's description names the account.
 */
@Composable
internal fun AvatarBadge(
    initials: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,
    fill: Color = avatarFill(colorIndex),
    symbol: DrawableResource? = null,
    content: Color = OtusColors.text1,
) {
    Box(
        modifier
            .size(AvatarTokens.badgeSize)
            .background(OtusColors.bgRegular, CircleShape)
            .padding(AvatarTokens.badgeRing)
            .border(Sizes.borderWidth, OtusColors.bgLight, CircleShape)
            .padding(Sizes.borderWidth)
            .clip(CircleShape)
            .background(fill),
        contentAlignment = Alignment.Center,
    ) {
        when {
            image != null -> AvatarPhoto(image, Modifier.matchParentSize())
            symbol != null -> AvatarSymbol(symbol, AvatarTokens.badgeSize, content)
            else -> BadgeInitials(initials, content)
        }
    }
}

@Composable
private fun BadgeInitials(
    initials: String,
    color: Color,
) {
    Text(
        initials,
        color = color,
        fontSize = AvatarTokens.badgeInitialsSize,
        lineHeight = AvatarTokens.badgeInitialsSize,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
    )
}
