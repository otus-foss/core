package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.ChoiceSheetTokens
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

/**
 * The modal sheet every sheet shares: the scrim, the sliding `bgDark` surface, and swiping down.
 * [content] gets `close`, which slides the sheet away and runs [onDismiss], `done`, which slides
 * it away and runs [onDone], and whether it is already closing.
 */
@Composable
internal fun SheetSurface(
    title: String,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    content: @Composable ColumnScope.(close: () -> Unit, done: () -> Unit, closing: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val motionScale = LocalMotionScale.current
    val position = remember { SheetPosition(motionScale) }
    val parent = LocalSheetHeight.current
    val held = remember { HeldHeight(parent?.px ?: 0) }
    // Read when the slide ends, so a draft changed during the animation is the one finished.
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnDone by rememberUpdatedState(onDone)
    val close: () -> Unit = { scope.launch { position.hide { currentOnDismiss() } } }
    val done: () -> Unit = { scope.launch { position.hide { currentOnDone() } } }
    val pull = LocalSheetPull.current
    val flingVelocity = with(density) { ChoiceSheetTokens.dismissVelocity.toPx() }
    LaunchedEffect(Unit) {
        if (pull == null) {
            position.show(MotionCurve.Enter)
        } else {
            // The sheet rises with the finger pulling it, and settles once the finger lets go.
            val released =
                snapshotFlow { Triple(pull.pulled, pull.releasedAt, position.height) }
                    .onEach { (pulled) -> position.pullTo(pulled) }
                    .mapNotNull { (_, released) -> released }
                    .first()
            position.releasePull(released, flingVelocity, close)
        }
    }
    Dialog(close, properties = selectionSheetDialogProperties()) {
        CompositionLocalProvider(LocalDensity provides density) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                SheetScrim(title, visible = 1f - position.hidden, onClick = close)
                Surface(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = SheetTokens.maxWidth)
                        .fillMaxWidth()
                        .heightIn(max = maxHeight * SheetTokens.HEIGHT_FRACTION)
                        .holdHeight(held)
                        .onSizeChanged { position.height = it.height }
                        .offset { IntOffset(0, (position.hidden * position.height).roundToInt()) }
                        .semantics { paneTitle = title },
                    shape = RoundedCornerShape(topStart = SheetTokens.radius, topEnd = SheetTokens.radius),
                    color = OtusColors.bgDark,
                    contentColor = OtusColors.text1,
                ) {
                    ProvideTextStyle(panelTextStyle()) {
                        Column(
                            Modifier
                                .swipeToDismiss(position, close)
                                .windowInsetsPadding(WindowInsets.safeDrawing.only(BottomAndSides)),
                        ) {
                            // A sheet opened from this one slides up by itself.
                            CompositionLocalProvider(
                                LocalSheetHeight provides held,
                                LocalSheetPull provides null,
                            ) { content(close, done, position.closing) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetScrim(
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
 * Never lets the sheet get shorter than [held], the tallest it has been, so its top stays put when
 * the keyboard closes and the bottom padding shrinks. It measures in the same pass as that padding,
 * so the two cannot drift apart for a frame.
 */
private fun Modifier.holdHeight(held: HeldHeight): Modifier =
    layout { measurable, constraints ->
        val placeable =
            measurable.measure(
                constraints.copy(minHeight = held.px.coerceIn(constraints.minHeight, constraints.maxHeight)),
            )
        held.px = maxOf(held.px, placeable.height)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

private val BottomAndSides = WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal

/**
 * The tallest a sheet has been, in px, starting from the sheet it was opened from. It grows during
 * layout, which must not write snapshot state.
 */
private class HeldHeight(
    var px: Int,
)

/** The held height of the sheet whose content is composing, so a sheet opened from it starts as tall. */
private val LocalSheetHeight = compositionLocalOf<HeldHeight?> { null }

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
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = ChoiceSheetTokens.handleTop)
                .size(ChoiceSheetTokens.handleWidth, ChoiceSheetTokens.handleHeight)
                .clip(CircleShape)
                .background(OtusColors.bgLight),
        )
        leadingAction?.let { Box(Modifier.align(Alignment.CenterStart)) { it() } }
        Text(
            title,
            Modifier.align(Alignment.Center).semantics { heading() },
            fontSize = ChoiceSheetTokens.titleSize,
            fontWeight = FontWeight.SemiBold,
        )
        onDone?.let {
            SheetHeaderAction(
                doneLabel,
                it,
                Modifier.align(Alignment.CenterEnd),
                color = OtusColors.accentLight,
                enabled = doneEnabled,
            )
        }
    }
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
            .heightIn(min = PanelTokens.touchTarget)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, RoundedCornerShape(SheetTokens.rowRadius))
            .padding(horizontal = SheetTokens.inset),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (enabled) color else OtusColors.text3, fontWeight = FontWeight.SemiBold) }
}
