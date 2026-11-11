package com.otusfoss.core.designsystem.shared.pages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otusfoss.core.designsystem.shared.panels.components.ChipPlacement
import com.otusfoss.core.designsystem.shared.panels.components.ContextPanel
import com.otusfoss.core.designsystem.shared.panels.components.ContextPanelScope
import com.otusfoss.core.designsystem.shared.panels.components.ExpandingSearch
import com.otusfoss.core.designsystem.shared.panels.components.PageSearch
import com.otusfoss.core.designsystem.shared.panels.components.rememberSearchExpansion
import com.otusfoss.core.designsystem.shared.panels.elements.PanelNavigation
import com.otusfoss.core.designsystem.shared.panels.elements.PanelTitle
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A page with a control panel header and a scrolling body. The leading chevron runs [onNavigation],
 * for example to open the sidebar on a top-level page. [chips], such as a
 * [PickerChip][com.otusfoss.core.designsystem.shared.selection.components.PickerChip], sit across the
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
    PageLayout(PageHeader(title, navigationLabel, onNavigation, chips, search), footer, backdrop = null, content)
}

/**
 * Something drawn edge to edge at the top of a [Subpage]'s body, which scrolls with it. Its top
 * tucks under the panel's rounded corners, so it reads as the panel's continuation.
 * [panelOverlay] draws over the panel at its size, such as to turn it into something else.
 */
public class PageBackdrop(
    public val content: @Composable () -> Unit,
    public val panelOverlay: @Composable BoxScope.() -> Unit = {},
)

/** The panel's parts: the title, the chevron's label and action, and the optional chips and search. */
private class PageHeader(
    val title: String,
    val navigationLabel: String,
    val onNavigation: () -> Unit,
    val chips: (@Composable ContextPanelScope.() -> Unit)?,
    val search: PageSearch?,
)

@Composable
private fun PageLayout(
    header: PageHeader,
    footer: (@Composable ColumnScope.() -> Unit)?,
    backdrop: PageBackdrop?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val search = header.search
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
            Box(Modifier.zIndex(1f)) {
                PagePanel(header, searching, { if (it) searching = true else closeSearch() }, expansion)
                backdrop?.panelOverlay?.invoke(this)
            }
            Box(Modifier.weight(1f).then(if (backdrop != null) Modifier.underPanelCorners() else Modifier)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    backdrop?.content?.invoke()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .pageSideInset()
                            .padding(vertical = PageTokens.contentInset),
                        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
                        content = content,
                    )
                }
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

/** The control panel with the chevron, then the title or, with a search, the search that grows over it. */
@Composable
private fun PagePanel(
    header: PageHeader,
    searching: Boolean,
    onSearching: (Boolean) -> Unit,
    expansion: () -> Float,
) {
    ContextPanel(
        placement = if (header.chips != null) ChipPlacement.Overlapping else ChipPlacement.None,
        navigation = { PanelNavigation(header.navigationLabel, header.onNavigation) },
        chips = header.chips ?: {},
    ) {
        val search = header.search
        if (search == null) {
            PanelTitle(header.title, Modifier.padding(start = PanelTokens.titleStart))
        } else {
            ExpandingSearch(header.title, search, searching, onSearching, expansion)
        }
    }
}

/** Moves the body up under the panel's rounded corners and makes it that much taller. */
private fun Modifier.underPanelCorners(): Modifier =
    layout { measurable, constraints ->
        val overlap = PanelTokens.bottomRadius.roundToPx()
        val placeable =
            measurable.measure(
                constraints.copy(
                    minHeight = constraints.minHeight + overlap,
                    maxHeight =
                        constraints.maxHeight + overlap,
                ),
            )
        layout(placeable.width, placeable.height - overlap) { placeable.place(0, -overlap) }
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
 * [footer], and [search] work as on [PageScaffold]; a [backdrop] runs edge to edge under the panel.
 */
@Composable
public fun Subpage(
    title: String,
    onBack: () -> Unit,
    chips: (@Composable ContextPanelScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    search: PageSearch? = null,
    backdrop: PageBackdrop? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = onBack,
    )
    PageLayout(PageHeader(title, "Back", onBack, chips, search), footer, backdrop, content)
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
