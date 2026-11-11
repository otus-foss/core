package com.otusfoss.core.designsystem.shared.feedback.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_help
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.overlays.components.SheetFrame
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.feedback.InfoTipTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A small `text2` question mark, with nothing behind it, that opens [info] in a sheet. It sits after a title,
 * so a page shows only what the user needs at a glance and the rest waits one tap away.
 */
@Composable
public fun InfoTip(
    info: InfoText,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    // No circle behind it: only the question mark shows, inside a full touch target.
    Box(
        modifier
            .size(Sizes.touchTarget)
            .clickable(role = Role.Button, onClickLabel = "Open") { open = true }
            .semantics { contentDescription = "About ${info.title}" },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_help, size = InfoTipTokens.symbolSize)
        }
    }
    if (open) {
        SheetFrame(info.title, { open = false }, done = null, closeButton = true) {
            Column(
                Modifier.fillMaxWidth().padding(
                    start = SheetTokens.inset,
                    end = SheetTokens.inset,
                    bottom = SheetTokens.inset,
                ),
                verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
            ) {
                info.paragraphs.forEach { Text(it, color = OtusColors.text2) }
            }
        }
    }
}
