package com.otus.designsystem.shared.rows.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens

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
    val shape = RoundedCornerShape(SettingTokens.valueRadius)
    Box(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .semantics { accessibilityLabel?.let { contentDescription = it } }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            Modifier
                .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
                .controlPressFeedback(interactions, shape)
                .padding(
                    horizontal = SettingTokens.valuePaddingHorizontal,
                    vertical = SettingTokens.valuePaddingVertical,
                ),
            maxLines = 1,
        )
    }
}
