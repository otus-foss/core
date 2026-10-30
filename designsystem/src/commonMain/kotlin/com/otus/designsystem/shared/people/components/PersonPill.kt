package com.otus.designsystem.shared.people.components

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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.shared.controls.elements.ChipDivider
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A person's [avatar] and [name] in an outlined pill, read as one item by screen readers, as
 * [description] when given. A [detail], such as the date of a quoted email, follows the name after
 * a divider line, inside the same pill, with a button that shows it in full when it has one. With
 * [onClick] it is a button inside a 48 dp touch target, which makes it take more room than the pill
 * it shows.
 */
@Composable
internal fun PersonPill(
    name: String,
    avatar: ChipAvatar,
    size: PersonPillSize,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    detail: PillDetail? = null,
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
                .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
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
    detail: PillDetail? = null,
) {
    val small = size == PersonPillSize.Small
    Row(
        modifier
            .height(size.height)
            .background(if (small) OtusColors.bgDark else Color.Transparent, CircleShape)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
            .then(action)
            .semantics { if (description != null) contentDescription = description }
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
        if (detail != null) {
            ChipDivider(selected = false)
            Text(
                detail.text,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = size.textSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val toggle = detail.onToggle
            if (toggle != null) DateToggle(detail.expanded, toggle)
        }
    }
}

/**
 * The [text] after a pill's divider, such as a date, shown in full while [expanded]. [onToggle]
 * switches it; null leaves out the button, as for a date that has nothing more to show.
 */
internal class PillDetail(
    val text: String,
    val expanded: Boolean = false,
    val onToggle: (() -> Unit)? = null,
)

/**
 * A small chevron button after a date that shows it in full, pointing on while the date is short
 * and back once it is [expanded]. Compose widens its touch area to 48 dp without making it taller.
 */
@Composable
internal fun DateToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
        Box(
            Modifier
                .clickable(role = Role.Button, onClick = onToggle)
                .semantics { contentDescription = if (expanded) "Show short date" else "Show full date" }
                .rotate(if (expanded) HALF_TURN else 0f),
        ) { Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true, size = ReadTokens.dateToggleSize) }
    }
}

private const val HALF_TURN = 180f
