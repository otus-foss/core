package com.otusfoss.core.designsystem.mail.attachments.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_download
import com.otusfoss.core.designsystem.shared.controls.elements.PlainAction
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens

/**
 * The files of a message as an open list that sits straight on a panel: the [summary], such as
 * "3 attachments · 4.2 MB", with "Save all", then one row per file, which run the [actions]. It
 * draws no card and does not fold; an [AttachmentStrip] does both.
 */
@Composable
public fun AttachmentList(
    files: List<AttachmentFile>,
    summary: String,
    actions: AttachmentActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = ReadTokens.attachmentHeight).topHairline(OtusColors.strokeBgDark),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryText(summary, StripTokens.titleSize, bold = true, Modifier.weight(1f))
            if (files.size > 1) PlainAction("Save all", Res.drawable.symbol_download, actions.onSaveAll)
        }
        files.forEachIndexed { index, file ->
            FileRow(file, { actions.onOpen(index) }, { actions.onSave(index) }, PaddingValues(0.dp))
        }
    }
}
