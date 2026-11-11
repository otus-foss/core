package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

@Composable
public fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    contentPadding: Dp = SheetTokens.inset,
    labelContent: (@Composable () -> Unit)? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        modifier
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions),
        shape = CircleShape,
        color =
            when {
                !primary -> OtusColors.bgDark

                enabled -> OtusColors.accentDark

                // Grey, so a primary button that waits for input still reads as a button.
                else -> OtusColors.bgLight
            },
        border =
            BorderStroke(
                Sizes.borderWidth,
                if (primary && enabled) OtusColors.accentRegular else OtusColors.strokeBgDark,
            ),
    ) {
        Box(
            Modifier
                .heightIn(min = Sizes.touchTarget)
                .padding(horizontal = contentPadding, vertical = Spacing.iconGap),
            contentAlignment = Alignment.Center,
        ) {
            if (labelContent == null) {
                Text(label, style = panelTextStyle(), color = if (enabled) OtusColors.text1 else OtusColors.text3)
            } else {
                labelContent()
            }
        }
    }
}
