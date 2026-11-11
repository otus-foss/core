package com.otusfoss.core.designsystem.shared.rows.elements

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens

/**
 * A small outlined button at the end of a settings row, such as "Remove". [accessibilityLabel]
 * names it when the visible [label] alone is ambiguous, for example "Remove Ada".
 */
@Composable
public fun RowButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(SettingTokens.valueRadius)
    Box(
        modifier
            .heightIn(min = Sizes.touchTarget)
            .semantics { if (accessibilityLabel != null) contentDescription = accessibilityLabel }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            Modifier
                .strokeBorder(Sizes.borderWidth, OtusColors.bgLight, shape)
                .controlPressFeedback(interactions, shape)
                .padding(
                    horizontal = SettingTokens.valuePaddingHorizontal,
                    vertical = SettingTokens.valuePaddingVertical,
                ),
            maxLines = 1,
        )
    }
}
