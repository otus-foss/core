package com.otus.designsystem.shared.actionbar.components

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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * A minimized bar's round button at its start, ringed like the full bar's secondary group. It
 * opens [actions] in a [ContextMenu][com.otus.designsystem.shared.overlays.components.ContextMenu] that sits one
 * bar gap above it, lined up with its start edge.
 */
@Composable
internal fun BarMoreButton(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier,
) {
    val more = BarAction("More actions", Res.drawable.symbol_more_horiz, menu = actions) {}
    val menu = rememberBarMenu()
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(ActionBarTokens.height)
            .semantics { contentDescription = more.label }
            .clickable(interactions, null, role = Role.Button, onClick = menu.click(more))
            .controlPressFeedback(interactions, CircleShape)
            .background(OtusColors.bgDark, CircleShape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
            Symbol(more.symbol, size = ActionBarTokens.symbolSize)
        }
        menu.Menu(more)
    }
}
