package com.otus.designsystem.shared.dates.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.ChipFrame
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A short date in a pill beside a [Chip][com.otus.designsystem.shared.controls.elements.Chip], such as "Yesterday"
 * next to the sender. The label is in
 * regular `text2` and never shortens, so the chips beside it give way first. Screen readers read
 * [description], such as the full timestamp, as its name.
 */
@Composable
public fun DatePill(
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected = null,
        enabled = true,
        fill = OtusColors.bgDark,
    ) {
        Text(label, style = panelTextStyle(), color = OtusColors.text2, maxLines = 1, softWrap = false)
    }
}
