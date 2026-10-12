package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

/**
 * A minimized bar's only button: [primary]'s icon in a circle as tall as the bar, filled like the
 * full bar's primary. Its description names the [context] too, since nothing else shows it.
 */
@Composable
internal fun RoundPrimary(
    primary: BarAction,
    context: BarContext?,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(ActionBarTokens.height)
            .background(if (primary.enabled) OtusColors.accentDark else OtusColors.bgDark, CircleShape)
            .border(
                PanelTokens.borderWidth,
                if (primary.enabled) OtusColors.accentRegular else OtusColors.strokeBgDark,
                CircleShape,
            ).semantics { contentDescription = listOfNotNull(primary.label, context?.description).joinToString(", ") }
            .clickable(interactions, null, enabled = primary.enabled, role = Role.Button, onClick = primary.onClick)
            .controlPressFeedback(interactions),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (primary.enabled) OtusColors.text1 else OtusColors.text3,
        ) {
            Symbol(primary.symbol, primary.mirrorInRtl, ActionBarTokens.symbolSize)
        }
    }
}
