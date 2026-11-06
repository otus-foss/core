package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A modal sheet that slides up from the bottom edge, titled [title]. Its [done] action, the scrim,
 * Back, and swiping down anywhere on it all slide it away; then [done] runs its action and the
 * others run [onDismiss]. Without a [closeButton], the title is centered and [done] is a text button
 * at the header's end, with [leadingAction] opposite it on the same slide-away-then-done path. With
 * a [closeButton], the title starts the header and a ✕ ends it, for choices that act at once, and
 * [done] becomes a full-width primary button under [content]. The sheet is as tall as its content,
 * up to most of the screen; content with a weighted child fills that height. The keyboard lifts the
 * content, and the sheet never gets shorter than it has been, so its top stays put when the keyboard
 * closes. A sheet opened from this one's content starts at least as tall.
 */
@Composable
public fun SheetFrame(
    title: String,
    onDismiss: () -> Unit,
    done: SheetConfirm? = SheetConfirm("Done", onDismiss),
    closeButton: Boolean = false,
    leadingAction: (@Composable (done: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetSurface(title, onDismiss, onDone = { done?.onConfirm?.invoke() }) { close, finish, closing ->
        if (closeButton) {
            ClosableHeader(title, close)
            content()
            if (done != null) ConfirmButton(done, finish, closing)
        } else {
            SheetHeader(
                title,
                onDone = done?.let { finish },
                doneLabel = done?.label.orEmpty(),
                doneEnabled = done?.enabled != false && !closing,
                leadingAction = leadingAction?.let { action -> { action(finish) } },
            )
            content()
        }
    }
}

/** The dimmed backdrop of a sheet or dialog at [visible] strength; tapping it runs [onClick]. */
@Composable
internal fun SheetScrim(
    title: String,
    visible: Float,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(OtusColors.scrim.copy(alpha = SheetTokens.SCRIM_ALPHA * visible))
            .semantics { contentDescription = "Dismiss $title" }
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick),
    )
}

/**
 * The grab handle, the centered [title], [leadingAction] at the start, and [doneLabel] at the end
 * unless [onDone] is null.
 */
@Composable
internal fun SheetHeader(
    title: String,
    onDone: (() -> Unit)?,
    modifier: Modifier = Modifier,
    doneEnabled: Boolean = true,
    doneLabel: String = "Done",
    leadingAction: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxWidth().height(ChoiceSheetTokens.headerHeight)) {
        SheetHandle(Modifier.align(Alignment.TopCenter))
        if (leadingAction != null) Box(Modifier.align(Alignment.CenterStart)) { leadingAction() }
        Text(
            title,
            Modifier.align(Alignment.Center).semantics { heading() },
            fontSize = ChoiceSheetTokens.titleSize,
            fontWeight = FontWeight.SemiBold,
        )
        if (onDone != null) {
            SheetHeaderAction(
                doneLabel,
                onDone,
                Modifier.align(Alignment.CenterEnd),
                color = OtusColors.text1,
                enabled = doneEnabled,
            )
        }
    }
}

/** The grab handle at the top of a sheet's header, placed by [modifier]. */
@Composable
internal fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(top = ChoiceSheetTokens.handleTop)
            .size(ChoiceSheetTokens.handleWidth, ChoiceSheetTokens.handleHeight)
            .clip(CircleShape)
            .background(OtusColors.bgLight),
    )
}

/**
 * A text button in the sheet header, with a full touch target and the shared press feedback, such
 * as a [SheetFrame] leading action.
 */
@Composable
public fun SheetHeaderAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = OtusColors.text1,
    enabled: Boolean = true,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .heightIn(min = Sizes.touchTarget)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius))
            .padding(horizontal = SheetTokens.inset),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (enabled) color else OtusColors.text3, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun ClosableHeader(
    title: String,
    onClose: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        SheetHandle(Modifier.align(Alignment.TopCenter))
        Row(
            Modifier.fillMaxWidth().padding(
                start = SheetTokens.inset,
                top = SheetTokens.gap,
                end = Spacing.iconGap,
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

@Composable
private fun ConfirmButton(
    confirm: SheetConfirm,
    done: () -> Unit,
    closing: Boolean,
) {
    ActionButton(
        confirm.label,
        done,
        Modifier.fillMaxWidth().padding(SheetTokens.inset),
        primary = true,
        enabled = confirm.enabled && !closing,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.iconGap * 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = confirm.icon
            if (icon != null) Symbol(icon)
            Text(confirm.label, color = if (confirm.enabled) OtusColors.text1 else OtusColors.text3)
        }
    }
}
