package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otus.designsystem.shared.overlays.elements.Scrim
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.ChoiceSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
        if (pull == null) position.show(MotionCurve.Enter) else position.follow(pull, flingVelocity, close)
    }
    Dialog(close, properties = sheetDialogProperties()) {
        CompositionLocalProvider(LocalDensity provides density) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Scrim("Dismiss $title", visible = 1f - position.hidden, onClick = close)
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
                    shape = SmoothCornerShape(topStart = SheetTokens.radius, topEnd = SheetTokens.radius),
                    color = OtusColors.bgDark,
                    contentColor = OtusColors.text1,
                ) {
                    SheetBody(position, held, close) { content(close, done, position.closing) }
                }
            }
        }
    }
}

@Composable
private fun SheetBody(
    position: SheetPosition,
    held: HeldHeight,
    close: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvideTextStyle(panelTextStyle()) {
        Column(
            Modifier
                .swipeToDismiss(position, close)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(BottomAndSides)),
        ) {
            // A sheet opened from this one slides up by itself.
            CompositionLocalProvider(LocalSheetHeight provides held, LocalSheetPull provides null) { content() }
        }
    }
}

/**
 * Never lets the sheet get shorter than [held], the tallest it has been, so its top stays put when
 * the keyboard closes and the bottom padding shrinks. It measures in the same pass as that padding,
 * so the two cannot drift apart for a frame.
 */
private fun Modifier.holdHeight(held: HeldHeight): Modifier =
    layout { measurable, constraints ->
        val minHeight = held.px.coerceIn(constraints.minHeight, constraints.maxHeight)
        val placeable = measurable.measure(constraints.copy(minHeight = minHeight))
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
 * Rises with the finger behind [pull], then settles once it lets go: open after an upward fling of
 * [flingVelocity] or more than half shown, else [close].
 */
private suspend fun SheetPosition.follow(
    pull: SheetPull,
    flingVelocity: Float,
    close: () -> Unit,
) {
    val released =
        snapshotFlow { Triple(pull.pulled, pull.releasedAt, height) }
            .onEach { (pulled) -> pullTo(pulled) }
            .mapNotNull { (_, released) -> released }
            .first()
    releasePull(released, flingVelocity, close)
}

/** Android's window-inset option is absent from the iOS dialog API. */
internal expect fun sheetDialogProperties(): DialogProperties
