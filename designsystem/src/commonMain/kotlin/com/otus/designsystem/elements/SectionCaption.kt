package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A `text2` caption over a group of rows that does not collapse, such as "Folders" in the sidebar
 * or "Pinned" in a sheet. [action], such as an icon button that adds a row, sits at the end.
 */
@Composable
public fun SectionCaption(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = PanelTokens.touchTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier
                .weight(1f)
                .padding(horizontal = SidebarTokens.entryPadding)
                .semantics { heading() },
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = SidebarTokens.detailSize,
            lineHeight = SidebarTokens.detailLineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        action?.invoke()
    }
}
