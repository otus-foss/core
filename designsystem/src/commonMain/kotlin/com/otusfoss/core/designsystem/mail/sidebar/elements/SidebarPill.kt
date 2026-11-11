package com.otusfoss.core.designsystem.mail.sidebar.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrame
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrameStyle
import com.otusfoss.core.designsystem.shared.controls.elements.ChipText
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.typography.TypeScale
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A built-in mailbox in the sidebar: a [Chip][com.otusfoss.core.designsystem.shared.controls.elements.Chip]
 * filled `bgRegular` to stand apart from the sidebar's `bgDark`, with an optional [count] after the
 * label in smaller `text2`.
 */
@Composable
internal fun SidebarPill(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    ChipFrame(onClick, modifier, selected, enabled = true, style = ChipFrameStyle(OtusColors.bgRegular)) {
        Symbol(icon)
        ChipText(label)
        if (count != null) {
            Text(
                count,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = TypeScale.label,
                maxLines = 1,
            )
        }
    }
}
