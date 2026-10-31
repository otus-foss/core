package com.otus.designsystem.shared.pages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.shared.panels.components.ChipPlacement
import com.otus.designsystem.shared.panels.components.ContextPanel
import com.otus.designsystem.shared.panels.components.ContextPanelScope
import com.otus.designsystem.shared.panels.components.ExpandingSearch
import com.otus.designsystem.shared.panels.components.PageSearch
import com.otus.designsystem.shared.panels.components.rememberSearchExpansion
import com.otus.designsystem.shared.panels.elements.PanelNavigation
import com.otus.designsystem.shared.panels.elements.PanelTitle
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A page with a control panel header and a scrolling body. The leading chevron runs [onNavigation],
 * for example to open the sidebar on a top-level page. [chips], such as a
 * [PickerChip][com.otus.designsystem.shared.selection.components.PickerChip], sit across the
 * panel's edge. With a [search], a search button at the header's end grows into a field over the
 * title, and the body dims until something is typed; back or ✕ closes it. [footer] stays at the
 * bottom below the body, so its primary action is always visible however long the body grows.
 */
@Composable
public fun PageScaffold(
    title: String,
    navigationLabel: String,
    onNavigation: () -> Unit,
    chips: (@Composable ContextPanelScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    search: PageSearch? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    val expansion = rememberSearchExpansion(searching)
    val closeSearch = {
        search?.onQueryChange("")
        searching = false
    }
    if (search != null) {
        NavigationBackHandler(
            state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
            isBackEnabled = searching,
            onBackCompleted = closeSearch,
        )
    }
    ProvideTextStyle(panelTextStyle()) {
        Column(Modifier.fillMaxSize().belowControlPanel()) {
            ContextPanel(
                placement = if (chips != null) ChipPlacement.Overlapping else ChipPlacement.None,
                navigation = { PanelNavigation(navigationLabel, onNavigation) },
                chips = chips ?: {},
            ) {
                if (search == null) {
                    PanelTitle(title, Modifier.padding(start = PanelTokens.titleStart))
                } else {
                    ExpandingSearch(
                        title,
                        search,
                        searching,
                        { if (it) searching = true else closeSearch() },
                        expansion,
                    )
                }
            }
            Box(Modifier.weight(1f).pageSideInset()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState(),
                        ).padding(vertical = PageTokens.contentInset),
                    verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
                    content = content,
                )
                if (search != null && search.query.isEmpty()) SearchDim(expansion, closeSearch)
            }
            if (footer != null) {
                Column(
                    Modifier.pageSideInset().padding(bottom = PageTokens.pageInset),
                    verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
                    content = footer,
                )
            }
        }
    }
}

/** Dims the body while the search field is open and empty; a tap there closes the search. */
@Composable
private fun BoxScope.SearchDim(
    expansion: () -> Float,
    onClose: () -> Unit,
) {
    if (expansion() == 0f) return
    Box(
        Modifier
            .matchParentSize()
            .graphicsLayer { alpha = PanelTokens.SEARCH_DIM * expansion() }
            .background(OtusColors.scrim)
            .pointerInput(onClose) { detectTapGestures { onClose() } },
    )
}

/**
 * A child page. The chevron and the system back gesture both return to its parent. [chips],
 * [footer], and [search] work as on [PageScaffold].
 */
@Composable
public fun Subpage(
    title: String,
    onBack: () -> Unit,
    chips: (@Composable ContextPanelScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    search: PageSearch? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = onBack,
    )
    PageScaffold(
        title,
        navigationLabel = "Back",
        onNavigation = onBack,
        chips = chips,
        footer = footer,
        search = search,
        content = content,
    )
}

/** A titled group of rows on a page. */
@Composable
public fun PageSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap)) {
        Text(
            title,
            Modifier.padding(horizontal = PageTokens.textInset).semantics { heading() },
            color = OtusColors.text2,
            fontSize = PageTokens.captionSize,
        )
        content()
    }
}

/** Explanatory text on a page, aligned with the text inside full-width rows. */
@Composable
public fun PageNote(text: String) {
    Text(text, Modifier.padding(horizontal = PageTokens.textInset), color = OtusColors.text2)
}
