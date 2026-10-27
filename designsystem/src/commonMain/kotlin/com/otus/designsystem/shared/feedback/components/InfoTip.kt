package com.otus.designsystem.shared.feedback.components

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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_help
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.overlays.components.ClosableSheet
import com.otus.designsystem.theme.InfoTipTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens

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
            .size(PanelTokens.touchTarget)
            .clickable(role = Role.Button, onClickLabel = "Open") { open = true }
            .semantics { contentDescription = "About ${info.title}" },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_help, size = InfoTipTokens.symbolSize)
        }
    }
    if (open) {
        ClosableSheet(info.title, { open = false }) {
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
