package com.otus.designsystem.elements

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
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

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
) {
    Box(
        modifier
            .size(InboxTokens.badgeSize)
            .background(OtusColors.bgRegular, CircleShape)
            .padding(InboxTokens.badgeRing)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
            .padding(PanelTokens.borderWidth)
            .background(avatarFill(colorIndex), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            color = OtusColors.text1,
            fontSize = InboxTokens.badgeInitialsSize,
            lineHeight = InboxTokens.badgeInitialsSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
