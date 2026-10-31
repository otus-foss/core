package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.input.elements.SearchField
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/**
 * A search field in the control panel's header: the [query] in a page-colored pill, and a grey ✕
 * that clears it once there is something to clear. [modifier] reaches the text field, such as to
 * focus it.
 */
@Composable
public fun PanelSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(OtusColors.bgRegular, CircleShape)
            .padding(start = PanelTokens.searchPillStart),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(query, onQueryChange, placeholder, modifier.weight(1f), onSubmit)
        if (query.isNotEmpty()) {
            IconButton("Clear search", { onQueryChange("") }, look = IconButtonLook.Bare) {
                CompositionLocalProvider(
                    LocalContentColor provides OtusColors.text2,
                ) { Symbol(Res.drawable.symbol_close) }
            }
        }
    }
}
