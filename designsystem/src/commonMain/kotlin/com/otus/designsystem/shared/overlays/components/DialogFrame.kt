package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.overlays.elements.Scrim
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A centered popup over a scrim, with a title and a close button. Dismissing changes nothing.
 * Without [closeButton], the popup's own buttons, such as Cancel, are the way out besides the scrim.
 */
@Composable
internal fun DialogFrame(
    title: String,
    onDismiss: () -> Unit,
    closeButton: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalDensity provides density) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Scrim("Dismiss $title", visible = 1f, onClick = onDismiss)
                DialogSurface(title, onDismiss.takeIf { closeButton }, content)
            }
        }
    }
}

@Composable
private fun DialogHeading(
    title: String,
    onClose: (() -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f).semantics { heading() }, fontSize = SheetTokens.titleSize)
        if (onClose != null) {
            IconButton("Close", onClose, look = IconButtonLook.Plain) { Symbol(Res.drawable.symbol_close) }
        }
    }
}

@Composable
private fun DialogSurface(
    title: String,
    onClose: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        Modifier
            .safeDrawingPadding()
            .imePadding()
            .padding(SheetTokens.gap)
            .widthIn(max = PageTokens.dialogMaxWidth)
            .fillMaxWidth()
            .semantics { paneTitle = title },
        shape = SmoothCornerShape(SheetTokens.radius),
        color = OtusColors.bgDark,
        contentColor = OtusColors.text1,
        border = BorderStroke(Sizes.borderWidth, OtusColors.strokeBgDark),
    ) {
        ProvideTextStyle(panelTextStyle()) {
            Column(
                Modifier.padding(SheetTokens.inset),
                verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
            ) {
                DialogHeading(title, onClose)
                content()
            }
        }
    }
}
