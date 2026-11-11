package com.otusfoss.core.designsystem.shared.dates.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrame
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrameStyle
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A short date in a pill beside a [Chip][com.otusfoss.core.designsystem.shared.controls.elements.Chip],
 * such as "Yesterday" next to the sender. The label is in regular `text2` and never shortens, so
 * the chips beside it give way first. Screen readers read [description], such as the full
 * timestamp, as its name.
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
        style = ChipFrameStyle(OtusColors.bgDark),
    ) {
        Text(label, style = panelTextStyle(), color = OtusColors.text2, maxLines = 1, softWrap = false)
    }
}
