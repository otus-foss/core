package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.shared.rows.elements.SectionCaption
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/**
 * The latest mail with someone in a sheet, under a [title] such as "Recent" and a hairline: one row
 * per line, the [MailLine.current] one on the selected fill. Tapping a row runs [onOpen] with its
 * index. The rows' text lines up with the sheet's; their fill reaches past it.
 */
@Composable
public fun RecentMail(
    title: String,
    lines: List<MailLine>,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = SheetTokens.inset - PageTokens.textInset)
            .topHairline(OtusColors.strokeBgDark, inset = PageTokens.textInset),
    ) {
        // The caption pads its own text by the same inset as the rows.
        SectionCaption(title)
        lines.forEachIndexed { index, line -> MailLineRow(line, PageTokens.textInset) { onOpen(index) } }
    }
}
