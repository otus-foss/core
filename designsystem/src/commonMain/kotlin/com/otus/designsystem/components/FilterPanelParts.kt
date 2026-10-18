package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_left
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A [FilterPanel]'s top row: the [title] on the left, or a back button named after the page it
 * returns to while [onBack] is set, and a plain ✕ at the top right that calls [onClose]. The title
 * takes focus when it appears, so screen readers start at the panel.
 */
@Composable
public fun FilterPanelHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeDescription: String = "Close $title",
    onBack: (() -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(title, onBack == null) { focus.requestFocus() }
    Row(
        modifier.fillMaxWidth().height(FilterTokens.headerHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val titleModifier = Modifier.weight(1f).focusRequester(focus).semantics { heading() }
        if (onBack == null) {
            Text(
                title,
                titleModifier.padding(start = FilterTokens.titleInset).focusable(),
                style = panelTextStyle(),
                fontSize = FilterTokens.titleSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        } else {
            Row(titleModifier, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .heightIn(min = PanelTokens.touchTarget)
                        .clickable(role = Role.Button, onClick = onBack)
                        .padding(start = FilterTokens.labelInset, end = FilterTokens.titleInset),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true)
                    Text(title, style = panelTextStyle(), fontSize = FilterTokens.titleSize, maxLines = 1)
                }
            }
        }
        IconButton(closeDescription, onClose, outlined = false) {
            Symbol(Res.drawable.symbol_close, size = FilterTokens.closeIconSize)
        }
    }
}

/** One group of [FilterPanel] choices: a small `text3` [label] above its chips, which wrap. */
@Composable
public fun FilterSection(
    label: String,
    modifier: Modifier = Modifier,
    chips: @Composable FlowRowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FilterTokens.labelGap)) {
        Text(
            label,
            Modifier.padding(start = FilterTokens.labelInset).semantics { heading() },
            style = panelTextStyle(),
            color = OtusColors.text3,
            fontSize = FilterTokens.labelSize,
        )
        // Each chip's 48 dp touch target already keeps rows 12 dp apart, so rows add no gap of their own.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipGap), content = chips)
    }
}
