package com.otus.designsystem.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.FolderPath
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A [folder] to tick in a sheet, such as the folders a message is in: its glyph, its path with the
 * parents in `text3`, an optional `text3` [detail] such as "you're here", and a checkbox, since a
 * message may be in several folders. A system folder shows its outlined icon.
 */
@Composable
public fun FolderPickRow(
    folder: FolderName,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    PickRowFrame(
        PickMark(checked, multiple = true),
        onClick,
        modifier,
        leading = { FolderNameGlyph(folder, PanelTokens.iconSize) },
        trailing =
            detail?.let {
                {
                    Text(
                        it,
                        style = panelTextStyle(),
                        color = OtusColors.text3,
                        fontSize = ComposeTokens.detailSize,
                        maxLines = 1,
                    )
                }
            },
    ) { FolderPath(folder.path) }
}
