package com.otus.designsystem.elements

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.panelTextStyle

/**
 * A header line of a message being written that shows a choice made elsewhere, such as when it
 * sends: [label] in its column and the [value]. Tapping the line runs [onClick] to change it, and
 * the ✕ at its end runs [onClear], named [clearLabel] for screen readers.
 */
@Composable
public fun ComposeChoiceField(
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    clearLabel: String,
    modifier: Modifier = Modifier,
) {
    ComposeLine(label, modifier.clickable(onClickLabel = "Change", role = Role.Button, onClick = onClick)) {
        Text(value, Modifier.weight(1f), style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(clearLabel, onClear, outlined = false) { Symbol(Res.drawable.symbol_close) }
    }
}
