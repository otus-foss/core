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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

/**
 * A minimized bar's round button at its start, ringed like the full bar's secondary group. It
 * opens [actions] in a [ContextMenu] that sits one bar gap above it, lined up with its start edge.
 */
@Composable
internal fun BarMoreButton(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val interactions = remember { MutableInteractionSource() }
    val corner =
        with(LocalDensity.current) {
            val start = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 0f else ActionBarTokens.height.toPx()
            Offset(start, -ActionBarTokens.gap.toPx())
        }
    Box(
        modifier
            .size(ActionBarTokens.height)
            .semantics { contentDescription = "More actions" }
            .clickable(interactions, null, role = Role.Button) { open = true }
            .controlPressFeedback(interactions, CircleShape)
            .background(OtusColors.bgDark, CircleShape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
            Symbol(Res.drawable.symbol_more_horiz, size = ActionBarTokens.symbolSize)
        }
        if (open) ContextMenu(actions, corner) { open = false }
    }
}
