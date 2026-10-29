package com.otus.designsystem.shared.sidebar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.min
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import kotlinx.coroutines.launch

/**
 * App-independent navigation sidebar: a [header], such as [SidebarHeader], over the scrolling
 * [content], over a [footer], such as [SidebarFooter], that stays at the bottom. [title] names
 * the pane for screen readers. It is 336 dp wide, or narrower when that would leave less than
 * 56 dp of the page visible. It half-opens from the leading edge over the current page, and a
 * swipe back toward that edge closes it. Wrap the page in [SidebarSwipeArea] to open it by swiping.
 */
@Composable
public fun Sidebar(
    state: SidebarState,
    title: String,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val motionScale = LocalMotionScale.current
    SideEffect { state.motionScale = motionScale }
    val close: () -> Unit = { scope.launch { state.close() } }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = state.isOpen,
        onBackCompleted = close,
    )
    val showing = state.isOpen || state.progress > 0f
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // A focused field, such as search, would keep the keyboard over the sidebar.
    LaunchedEffect(showing) {
        if (showing) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    // One progress value drives the slide and the scrim, so a swipe or a reversal mid-way stays continuous.
    if (!showing) return
    val sign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val flingThreshold = with(LocalDensity.current) { SidebarTokens.flingVelocity.toPx() }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .draggable(
                rememberDraggableState { state.dragBy(it * sign) },
                Orientation.Horizontal,
                onDragStarted = { state.stop() },
                onDragStopped = { state.settle(it * sign, flingThreshold) },
            ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = state.progress }
                .background(OtusColors.scrim.copy(alpha = SidebarTokens.SCRIM_ALPHA))
                .semantics { contentDescription = "Close navigation" }
                .clickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    enabled = state.isOpen,
                    onClick = close,
                ),
        )
        Box(
            Modifier
                .fillMaxHeight()
                .width(min(SidebarTokens.width, maxWidth - SidebarTokens.pageVisible))
                .onSizeChanged { state.widthPx = it.width.toFloat() }
                .graphicsLayer {
                    // Slide fully past the leading screen edge.
                    translationX = -sign * size.width * (1f - state.progress)
                },
        ) {
            SidebarSurface(title, header, footer, content)
        }
    }
}

/** Flush with the top, leading, and bottom screen edges, so only the trailing edge, mid-screen, gets a stroke. */
private fun Modifier.trailingStroke(): Modifier =
    drawWithContent {
        drawContent()
        val width = PanelTokens.borderWidth.toPx()
        val x = if (layoutDirection == LayoutDirection.Rtl) width / 2 else size.width - width / 2
        drawLine(OtusColors.strokeBgDark, Offset(x, 0f), Offset(x, size.height), width)
    }

/** The trailing edge sits mid-screen, so it needs no system-bar or cutout padding. */
private val sidebarInsetSides = WindowInsetsSides.Top + WindowInsetsSides.Bottom + WindowInsetsSides.Start

@Composable
private fun SidebarSurface(
    title: String,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        Modifier.fillMaxSize().semantics { paneTitle = title }.trailingStroke(),
        color = OtusColors.bgDark,
        contentColor = OtusColors.text1,
    ) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(sidebarInsetSides))) {
            header()
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SidebarTokens.inset)
                    .padding(bottom = SidebarTokens.inset),
                content = content,
            )
            Box(Modifier.padding(horizontal = SidebarTokens.inset)) { footer() }
        }
    }
}
