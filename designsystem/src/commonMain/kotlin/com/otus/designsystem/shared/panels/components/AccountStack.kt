package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.lerp
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * The accounts or addresses a page shows mail from, as overlapping avatars at the end of the
 * control panel's header, each ringed in the panel's color. A tap runs [onClick], such as to open
 * the account switcher. As the panel [collapse]s, the stack folds under its first avatar and a
 * "+N" badge counts the rest.
 */
@Composable
public fun AccountStack(
    avatars: List<ChipAvatar>,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapse: () -> Float = { 0f },
) {
    val interactions = remember { MutableInteractionSource() }
    val shown = avatars.take(PanelTokens.CHIP_AVATAR_LIMIT)
    val hidden = avatars.size - 1
    Box(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .widthIn(min = PanelTokens.touchTarget)
            .semantics { contentDescription = description }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .padding(start = PanelTokens.headerGap, end = PanelTokens.stackEnd),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.controlPressFeedback(interactions).clearAndSetSemantics {}) {
            FoldingAvatars(shown, collapse)
            if (hidden > 0) PlusBadge(hidden, collapse)
        }
    }
}

/** Avatars that overlap by [PanelTokens.stackAvatarOverlap], sliding under the first as [collapse] grows. */
@Composable
private fun FoldingAvatars(
    avatars: List<ChipAvatar>,
    collapse: () -> Float,
) {
    Layout(
        content = {
            avatars.forEachIndexed { index, avatar ->
                PersonAvatar(
                    avatar,
                    PanelTokens.stackAvatarSize - PanelTokens.stackRing * 2,
                    Modifier
                        .graphicsLayer { if (index > 0) alpha = 1f - CollapseSteps.stack(collapse()) }
                        .panelRing(),
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val size = PanelTokens.stackAvatarSize.toPx()
        val step = lerp(size - PanelTokens.stackAvatarOverlap.toPx(), 0f, CollapseSteps.stack(collapse()))
        val width = (size + step * (placeables.size - 1).coerceAtLeast(0)).roundToInt()
        layout(width, size.roundToInt()) {
            // The first avatar draws on top, so the others fold in under it.
            placeables.forEachIndexed { index, placeable ->
                placeable.place((step * index).roundToInt(), 0, zIndex = -index.toFloat())
            }
        }
    }
}

@Composable
private fun BoxScope.PlusBadge(
    count: Int,
    collapse: () -> Float,
) {
    Text(
        "+$count",
        Modifier
            .align(Alignment.BottomEnd)
            .graphicsLayer {
                val shown = CollapseSteps.badge(collapse())
                alpha = shown
                scaleX = lerp(PanelTokens.BADGE_START_SCALE, 1f, shown)
                scaleY = scaleX
                translationX = PanelTokens.badgeOffset.toPx()
            }.panelRing()
            .background(OtusColors.bgLight, CircleShape)
            .padding(horizontal = PanelTokens.badgePadding),
        style = panelTextStyle(),
        color = OtusColors.text1,
        fontSize = PanelTokens.badgeTextSize,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
    )
}

/**
 * A ring in the panel's color around what follows, drawn behind it rather than over its edge. A
 * border on top would leave the content's anti-aliased edge showing outside it as a light hairline.
 */
private fun Modifier.panelRing(): Modifier =
    background(OtusColors.bgDark, CircleShape)
        .padding(PanelTokens.stackRing)
