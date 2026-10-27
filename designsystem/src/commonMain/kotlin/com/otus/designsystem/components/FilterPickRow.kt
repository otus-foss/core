package com.otus.designsystem.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check_circle_fill
import com.otus.designsystem.resources.symbol_radio_button_unchecked
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors

/**
 * A person, address, or account to pick on a filter page, [FilterTokens.pickRowHeight] tall: an
 * optional [leading] avatar, the [title], a `text2` [subtitle], and a circle at the end, filled
 * with a check in `text1` while [mark] is selected. [mark] also sets whether screen readers
 * hear a checkbox or a radio button. A null [onClick] shows the row disabled.
 */
@Composable
public fun FilterPickRow(
    title: String,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    PickRowFrame(
        mark,
        onClick,
        modifier.heightIn(min = FilterTokens.pickRowHeight),
        leading,
        markView = { FilterPickMark(mark.selected) },
    ) { PickRowText(title, subtitle) }
}

/** The check circle of a [FilterPickRow] and a [FilterFolderRow]. */
@Composable
internal fun FilterPickMark(selected: Boolean) {
    CompositionLocalProvider(LocalContentColor provides if (selected) OtusColors.text1 else OtusColors.text3) {
        Symbol(if (selected) Res.drawable.symbol_check_circle_fill else Res.drawable.symbol_radio_button_unchecked)
    }
}
