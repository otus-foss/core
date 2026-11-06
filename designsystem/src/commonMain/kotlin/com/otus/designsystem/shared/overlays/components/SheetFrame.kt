package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A modal sheet that slides up from the bottom edge, titled [title] with a [doneLabel] button. Done,
 * the scrim, Back, and swiping down anywhere on it all slide it away. Then Done runs [onDone] and
 * the others run [onDismiss]. [leadingAction] sits opposite Done and gets the same
 * slide-away-then-done path, so it can finish the sheet too. The sheet is as tall as its content,
 * up to most of the screen; content with a weighted child fills that height. The keyboard lifts the
 * content, and the sheet never gets shorter than it has been, so its top stays put when the keyboard
 * closes. A sheet opened from this one's content starts at least as tall.
 */
@Composable
public fun SheetFrame(
    title: String,
    onDismiss: () -> Unit,
    onDone: () -> Unit = onDismiss,
    doneEnabled: Boolean = true,
    doneLabel: String = "Done",
    leadingAction: (@Composable (done: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetSurface(title, onDismiss, onDone) { close, done, closing ->
        SheetHeader(
            title,
            onDone = done,
            doneLabel = doneLabel,
            doneEnabled = doneEnabled && !closing,
            leadingAction = leadingAction?.let { action -> { action(done) } },
        )
        content()
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
