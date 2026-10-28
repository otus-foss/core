package com.otus.designsystem.mail.attachments.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_download
import com.otus.designsystem.shared.controls.components.StripChevron
import com.otus.designsystem.shared.controls.elements.PlainAction
import com.otus.designsystem.shared.rows.components.Unfold
import com.otus.designsystem.shared.rows.components.foldedForm
import com.otus.designsystem.shared.rows.components.stripCardFrame
import com.otus.designsystem.shared.rows.components.unfoldedForm
import com.otus.designsystem.shared.rows.components.unfolding
import com.otus.designsystem.theme.tokens.content.ReadTokens

/**
 * The files of a message in a strip above the action bar, never inline in its text. Folded, it
 * shows overlapping thumbnails and the [summary], such as "3 attachments · 4.2 MB". As [unfold]
 * opens, the same card grows upward into a header with "Save all" and one row per file, which run
 * the [actions], while the folded line fades out; the rows scroll past a fixed height. A finger
 * can move [unfold] part of the way. Tapping the folded strip or the header runs [onToggle].
 */
@Composable
public fun AttachmentStrip(
    files: List<AttachmentFile>,
    summary: String,
    unfold: Unfold,
    onToggle: () -> Unit,
    actions: AttachmentActions,
    modifier: Modifier = Modifier,
) {
    // The unfold sets the height frame by frame, so the card must not animate it again.
    Box(modifier.stripCardFrame()) {
        val folded = unfold.fraction == 0f
        Column(
            Modifier
                .unfolding(unfold, ReadTokens.attachmentHeight)
                .unfoldedForm(unfold)
                .then(if (folded) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            AttachmentHeader(summary, onToggle, actions.onSaveAll)
            Column(Modifier.heightIn(max = ReadTokens.openStripMaxHeight).verticalScroll(rememberScrollState())) {
                files.forEachIndexed { index, file ->
                    FileRow(file, { actions.onOpen(index) }, { actions.onSave(index) })
                }
            }
        }
        if (unfold.fraction < 1f) {
            FoldedLine(
                files,
                summary,
                onToggle,
                Modifier
                    .align(Alignment.BottomStart)
                    .foldedForm(unfold),
            )
        }
    }
}

/** The folded strip: the thumbnails, the [summary], and a chevron, one button that runs [onToggle]. */
@Composable
private fun FoldedLine(
    files: List<AttachmentFile>,
    summary: String,
    onToggle: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier
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
