package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_left
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A page with a context panel header and a scrolling body. The leading chevron runs [onNavigation],
 * for example to open the sidebar on a top-level page. [chips], such as a [PickerChip], fill the
 * header's chip row. [footer] stays at the bottom below the body, so its primary action is always
 * visible however long the body grows.
 */
@Composable
public fun PageScaffold(
    title: String,
    navigationLabel: String,
    onNavigation: () -> Unit,
    chips: (@Composable ContextPanelScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvideTextStyle(panelTextStyle()) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(PageTokens.pageInset),
        ) {
            ContextPanel(
                // Chips sit inside the panel, as on a draft's header; a page without them keeps its plain header.
                placement = if (chips != null) ChipPlacement.Overlapping else ChipPlacement.Below,
                navigation = {
                    IconButton(navigationLabel, onNavigation, outlined = false) {
                        Symbol(
                            Res.drawable.symbol_chevron_left,
                            mirrorInRtl = true,
                            size = PanelTokens.navigationIconSize,
                        )
                    }
                },
                chips = chips ?: {},
            ) { Text(title, Modifier.semantics { heading() }) }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = PageTokens.contentInset),
                verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
                content = content,
            )
            footer?.let { Column(verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap), content = it) }
        }
    }
}

/**
 * A child page. The chevron and the system back gesture both return to its parent. [chips] fill the
 * header's chip row, and [footer] stays at the bottom, as on [PageScaffold].
 */
@Composable
public fun Subpage(
    title: String,
    onBack: () -> Unit,
    chips: (@Composable ContextPanelScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
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
