package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.otus.designsystem.theme.MotionTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A modal sheet that slides up from the bottom edge, titled [title] with a Done button. Done, the
 * scrim, Back, and swiping down anywhere on it all slide it away. Then Done runs [onDone] and the
 * others run [onDismiss]. [leadingAction] sits opposite Done and gets the same slide-away-then-done
 * path, so it can finish the sheet too.
 */
@Composable
public fun SheetFrame(
    title: String,
    onDismiss: () -> Unit,
    onDone: () -> Unit = onDismiss,
    doneEnabled: Boolean = true,
    leadingAction: (@Composable (done: () -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val position = remember { SheetPosition() }
    // Read when the slide ends, so a draft changed during the animation is the one finished.
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnDone by rememberUpdatedState(onDone)
    val close: () -> Unit = { scope.launch { position.hide { currentOnDismiss() } } }
    val done: () -> Unit = { scope.launch { position.hide { currentOnDone() } } }
    LaunchedEffect(Unit) { position.show(MotionTokens.enter) }
    Dialog(close, properties = selectionSheetDialogProperties()) {
        CompositionLocalProvider(LocalDensity provides density) {
            Box(Modifier.fillMaxSize().imePadding()) {
                SheetScrim(title, visible = 1f - position.hidden, onClick = close)
                Surface(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = SheetTokens.maxWidth)
                        .fillMaxWidth()
                        .fillMaxHeight(SheetTokens.HEIGHT_FRACTION)
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
                            SheetHeader(
                                title,
                                onDone = done,
                                doneEnabled = doneEnabled && !position.closing,
                                leadingAction = leadingAction?.let { action -> { action(done) } },
                            )
                            content()
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
            .background(OtusColors.bgDark.copy(alpha = SheetTokens.SCRIM_ALPHA * visible))
            .semantics { contentDescription = "Dismiss $title" }
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick),
    )
}

private val BottomAndSides = WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal

/** The grab handle, the centered [title], [leadingAction] at the start, and Done at the end. */
@Composable
internal fun SheetHeader(
    title: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    doneEnabled: Boolean = true,
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
        SheetHeaderAction(
            "Done",
            onDone,
            Modifier.align(Alignment.CenterEnd),
            color = OtusColors.accentLight,
            enabled = doneEnabled,
        )
    }
}

/** A text button in the sheet header, with a full touch target and the shared press feedback. */
@Composable
internal fun SheetHeaderAction(
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
