package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A text button in a header, with a full touch target and the shared press feedback. The [inset]
 * pads the label at both sides, so the press highlight surrounds it evenly.
 */
@Composable
internal fun TextAction(
    label: String,
    onClick: () -> Unit,
    inset: Dp,
    modifier: Modifier = Modifier,
    color: Color = OtusColors.text1,
    enabled: Boolean = true,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .heightIn(min = Sizes.touchTarget)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius))
            .padding(horizontal = inset),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (enabled) color else OtusColors.text3, fontWeight = FontWeight.SemiBold) }
}
