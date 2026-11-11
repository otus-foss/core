package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A round button around a symbol. A [badgeCount] above zero puts a small `accentDark` count on its
 * top end, such as the number of active filters on the filter button; the badge sits outside the
 * circle's clip, so it is never cut off. [look] sets what is behind the symbol.
 */
@Composable
public fun IconButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    look: IconButtonLook = IconButtonLook.Outlined,
    badgeCount: Int = 0,
    content: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(Sizes.touchTarget)
            .semantics { contentDescription = description }
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(Sizes.chipHeight)) {
            Surface(
                modifier =
                    Modifier
                        .size(Sizes.chipHeight)
                        .reportControlVisualBounds()
                        .controlPressFeedback(interactions),
                shape = CircleShape,
                color = if (look == IconButtonLook.Bare) Color.Transparent else OtusColors.bgDark,
                contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
                border =
                    if (look == IconButtonLook.Outlined) {
                        BorderStroke(Sizes.borderWidth, OtusColors.strokeBgDark)
                    } else {
                        null
                    },
            ) {
                Box(contentAlignment = Alignment.Center) { content() }
            }
            if (badgeCount > 0) CountDot(badgeCount, Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun CountDot(
    count: Int,
    modifier: Modifier,
) {
    Box(
        modifier
            .sizeIn(minWidth = ControlTokens.badgeSize, minHeight = ControlTokens.badgeSize)
            .background(OtusColors.accentDark, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            count.toString(),
            style = panelTextStyle(),
            fontSize = ControlTokens.badgeTextSize,
            lineHeight = ControlTokens.badgeTextSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/** What is behind an [IconButton]'s symbol. */
public enum class IconButtonLook {
    /** A `bgDark` circle inside a `strokeBgDark` outline, the default. */
    Outlined,

    /** A `bgDark` circle without an outline, such as in a panel's header. */
    Plain,

    /** Nothing, for a field or row that has its own fill. */
    Bare,
}
