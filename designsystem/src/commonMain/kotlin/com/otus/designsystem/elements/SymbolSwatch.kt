package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.AvatarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ThemeTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * One symbol to pick, such as an avatar icon, in the same ring as a selected [ColorSwatch]. [label]
 * names the symbol for screen readers.
 */
@Composable
public fun SymbolSwatch(
    symbol: DrawableResource,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(PanelTokens.touchTarget)
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onClick)
            .controlPressFeedback(interactions, CircleShape)
            .semantics { contentDescription = label }
            .then(if (selected) Modifier.border(ThemeTokens.swatchRing, OtusColors.text1, CircleShape) else Modifier)
            .padding(ThemeTokens.swatchRingSpace)
            .background(OtusColors.bgRegular, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
            Symbol(symbol, size = PanelTokens.touchTarget * AvatarTokens.SYMBOL_SCALE)
        }
    }
}
