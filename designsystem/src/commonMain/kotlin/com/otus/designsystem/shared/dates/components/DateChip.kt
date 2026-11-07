package com.otus.designsystem.shared.dates.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.dates.elements.DatePill
import com.otus.designsystem.shared.selection.components.Dropdown
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A [DatePill] with the short [label], such as "Yesterday", that shows the [full] date and time in
 * a dropdown while pressed open. Screen readers read [full] as the pill's name.
 */
@Composable
public fun DateChip(
    label: String,
    full: String,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        DatePill(label, full, { open = true })
        if (open) {
            Dropdown({ open = false }) {
                Text(
                    full,
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Sizes.touchTarget)
                        .wrapContentHeight()
                        .padding(horizontal = MenuTokens.rowPadding),
                    color = OtusColors.text1,
                    style = panelTextStyle(),
                )
            }
        }
    }
}
