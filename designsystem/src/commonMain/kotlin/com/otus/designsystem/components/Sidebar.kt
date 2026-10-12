package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.min
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle
import kotlinx.coroutines.launch

/**
 * App-independent navigation sidebar: a [rail] of accounts along the leading edge, and a panel
 * with [title] and [subtitle] over the scrolling [content]. It is 336 dp wide, or narrower when
 * that would leave less than 56 dp of the page visible. It half-opens from the leading edge over
 * the current page, and a swipe back toward that edge closes it. Wrap the page in
 * [SidebarSwipeArea] to open it by swiping. Fill [rail] with [SidebarRail], and [content] with
 * [com.otus.designsystem.elements.SidebarHeading] sections and
 * [com.otus.designsystem.elements.SidebarEntry] rows.
 */
@Composable
public fun Sidebar(
    state: SidebarState,
    title: String,
    subtitle: String,
    rail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
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
                .background(OtusColors.bgDark.copy(alpha = SidebarTokens.SCRIM_ALPHA))
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
            SidebarSurface(title, subtitle, rail, content)
        }
    }
}

/** Flush with the top, leading, and bottom screen edges, so only the trailing edge, mid-screen, gets a stroke. */
internal fun Modifier.trailingStroke(): Modifier =
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
    subtitle: String,
    rail: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        Modifier.fillMaxSize().semantics { paneTitle = title }.trailingStroke(),
        color = OtusColors.bgDark,
        contentColor = OtusColors.text1,
    ) {
        Row(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(sidebarInsetSides))) {
            rail()
            Column(Modifier.weight(1f)) {
                SidebarHeader(title, subtitle)
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SidebarTokens.inset)
                        .padding(bottom = SidebarTokens.inset),
                    content = content,
                )
            }
        }
    }
}

/** Shares the context panel's header height, so the title lines up with the page title. */
@Composable
private fun SidebarHeader(
    title: String,
    subtitle: String,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(PanelTokens.headerHeight)
            .padding(horizontal = SidebarTokens.inset + SidebarTokens.entryPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title,
            Modifier.semantics { heading() },
            style = panelTextStyle(),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            subtitle,
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = SidebarTokens.detailSize,
            lineHeight = SidebarTokens.detailLineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
