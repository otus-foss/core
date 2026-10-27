package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.ChoiceSheetTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens

/**
 * A modal sheet titled [title] at its start with a close ✕ at its end, for choices that act at
 * once or confirm at the bottom. A [confirm] adds a full-width primary button under [content];
 * it slides the sheet away and then runs. The ✕, the scrim, Back, and swiping down run [onDismiss].
 */
@Composable
public fun ClosableSheet(
    title: String,
    onDismiss: () -> Unit,
    confirm: SheetConfirm? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetSurface(title, onDismiss, onDone = { confirm?.onConfirm?.invoke() }) { close, done, closing ->
        ClosableHeader(title, close)
        content()
        confirm?.let {
            ActionButton(
                it.label,
                done,
                Modifier.fillMaxWidth().padding(SheetTokens.inset),
                primary = true,
                enabled = it.enabled && !closing,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap * 2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    it.icon?.let { icon -> Symbol(icon) }
                    Text(it.label, color = if (it.enabled) OtusColors.text1 else OtusColors.text3)
                }
            }
        }
    }
}

@Composable
private fun ClosableHeader(
    title: String,
    onClose: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = ChoiceSheetTokens.handleTop)
                .size(ChoiceSheetTokens.handleWidth, ChoiceSheetTokens.handleHeight)
                .clip(CircleShape)
                .background(OtusColors.bgLight),
        )
        Row(
            Modifier.fillMaxWidth().padding(
                start = SheetTokens.inset,
                top = SheetTokens.gap,
                end = PanelTokens.iconGap,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                Modifier.weight(1f).semantics { heading() },
                fontSize = SheetTokens.titleSize,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton("Close", onClose, look = IconButtonLook.Plain) { Symbol(Res.drawable.symbol_close) }
        }
    }
}
