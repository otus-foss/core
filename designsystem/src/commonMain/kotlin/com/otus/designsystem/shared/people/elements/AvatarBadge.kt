package com.otus.designsystem.shared.people.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.people.AvatarTokens
import com.otus.designsystem.theme.tokens.rows.InboxTokens
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
        badge?.let {
            Box(Modifier.align(Alignment.BottomEnd).offset(-InboxTokens.badgeOffset, -InboxTokens.badgeOffset)) { it() }
        }
    }
}

/**
 * A small avatar for a [BadgedAvatar]'s badge, such as the account a mail arrived in. A `bgRegular` ring
 * cuts it out of the avatar under it. It is decoration: the row's description names the account.
 */
@Composable
public fun AvatarBadge(
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
            .size(InboxTokens.badgeSize)
            .background(OtusColors.bgRegular, CircleShape)
            .padding(InboxTokens.badgeRing)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
            .padding(PanelTokens.borderWidth)
            .clip(CircleShape)
            .background(fill),
        contentAlignment = Alignment.Center,
    ) {
        when {
            image != null -> {
                Image(image, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            }

            symbol != null -> {
                CompositionLocalProvider(LocalContentColor provides content) {
                    Symbol(symbol, size = InboxTokens.badgeSize * AvatarTokens.SYMBOL_SCALE)
                }
            }

            else -> {
                Text(
                    initials,
                    color = content,
                    fontSize = InboxTokens.badgeInitialsSize,
                    lineHeight = InboxTokens.badgeInitialsSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}
