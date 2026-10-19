package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.PlainAction
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_download
import com.otus.designsystem.theme.ReadTokens

/**
 * The files of a message in a strip above the action bar, never inline in its text. Closed, it
 * shows overlapping thumbnails and the [summary], such as "3 attachments · 4.2 MB". [expanded],
 * the same card grows upward into a header with "Save all" and one row per file, which run the
 * [actions]; the rows scroll past a fixed height. Tapping the closed strip or the header runs
 * [onToggle].
 */
@Composable
public fun AttachmentStrip(
    files: List<AttachmentFile>,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    actions: AttachmentActions,
    modifier: Modifier = Modifier,
) {
    StripCard(modifier) {
        if (expanded) {
            AttachmentHeader(summary, onToggle, actions.onSaveAll)
            Column(Modifier.heightIn(max = ReadTokens.openStripMaxHeight).verticalScroll(rememberScrollState())) {
                files.forEachIndexed {
                    index,
                    file,
                    ->
                    FileRow(file, { actions.onOpen(index) }, { actions.onSave(index) })
                }
            }
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = ReadTokens.attachmentHeight)
                    .clickable(onClickLabel = "Show attachments", role = Role.Button, onClick = onToggle)
                    .padding(start = ReadTokens.stripStart, end = ReadTokens.slimEnd),
                horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThumbStack(files)
                SummaryText(summary, ReadTokens.summarySize, bold = false, Modifier.weight(1f))
                StripChevron(up = true)
            }
        }
    }
}

@Composable
private fun AttachmentHeader(
    summary: String,
    onFold: () -> Unit,
    onSaveAll: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = ReadTokens.attachmentHeight)
            .clickable(onClickLabel = "Fold", role = Role.Button, onClick = onFold)
            .padding(start = ReadTokens.slimStart, end = ReadTokens.slimEnd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryText(summary, ReadTokens.titleSize, bold = true, Modifier.weight(1f))
        PlainAction("Save all", Res.drawable.symbol_download, onSaveAll)
        StripChevron(up = false)
    }
}
