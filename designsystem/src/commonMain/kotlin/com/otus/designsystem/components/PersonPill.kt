package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.ChipDivider
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A person's [avatar] and [name] in an outlined pill, read as one item by screen readers, as
 * [description] when given. A [detail], such as the date of a quoted email, follows the name after
 * a divider line, inside the same pill. With [onClick] it is a button inside a 48 dp touch target,
 * which makes it take more room than the pill it shows.
 */
@Composable
internal fun PersonPill(
    name: String,
    avatar: ChipAvatar,
    size: PersonPillSize,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    detail: String? = null,
) {
    if (onClick == null) {
        PersonPillFrame(
            name,
            avatar,
            size,
            modifier.semantics(mergeDescendants = true) {},
            description,
            detail = detail,
        )
    } else {
        val interactions = remember { MutableInteractionSource() }
        Box(
            modifier
                .sizeIn(minWidth = PanelTokens.touchTarget, minHeight = PanelTokens.touchTarget)
                .clickable(interactions, null, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            PersonPillFrame(
                name,
                avatar,
                size,
                Modifier.controlPressFeedback(interactions),
                description,
                detail = detail,
            )
        }
    }
}

/**
 * The pill itself. [action] goes on the pill inside its border, for a caller that makes the pill
 * itself the button, such as a [RecipientChip] in a compact line of chips.
 */
@Composable
internal fun PersonPillFrame(
    name: String,
    avatar: ChipAvatar,
    size: PersonPillSize,
    modifier: Modifier,
    description: String?,
    action: Modifier = Modifier,
    detail: String? = null,
) {
    val small = size == PersonPillSize.Small
    Row(
        modifier
            .height(size.height)
            .background(if (small) OtusColors.bgDark else Color.Transparent, CircleShape)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
            .then(action)
            .semantics { description?.let { contentDescription = it } }
            .padding(start = size.start, end = PanelTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(size.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(avatar, size.avatar)
        Text(
            name,
            style = panelTextStyle(),
            color = if (small) OtusColors.text2 else Color.Unspecified,
            fontSize = size.textSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        detail?.let {
            ChipDivider(selected = false)
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = size.textSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
