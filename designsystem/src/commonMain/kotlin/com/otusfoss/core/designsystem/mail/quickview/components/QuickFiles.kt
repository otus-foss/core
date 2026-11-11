package com.otusfoss.core.designsystem.mail.quickview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.mail.attachments.components.AttachmentActions
import com.otusfoss.core.designsystem.mail.attachments.components.AttachmentFile
import com.otusfoss.core.designsystem.mail.attachments.components.FileIcon
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_download
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.QuickViewTokens

/**
 * A quick view module of a message's [files], one card each: its picture, its name on up to two
 * lines, and its detail. A tap opens a file and a long press saves it, as [actions] say. One or two
 * files share the row's width; more scroll sideways and fade at the end while cards wait there.
 * With two or more files, a Save all tile closes the row.
 */
@Composable
public fun QuickFiles(
    files: List<AttachmentFile>,
    actions: AttachmentActions,
    modifier: Modifier = Modifier,
) {
    val scrolls = files.size > QuickViewTokens.FILE_FILL_LIMIT
    val scroll = rememberScrollState()
    val row =
        when {
            !scrolls -> modifier
            scroll.canScrollForward -> modifier.fadeEnd().horizontalScroll(scroll)
            else -> modifier.horizontalScroll(scroll)
        }
    QuickModule(row, Arrangement.spacedBy(QuickViewTokens.tileGap)) {
        files.forEachIndexed { index, file ->
            FileCard(
                file,
                if (scrolls) Modifier.width(QuickViewTokens.fileCardWidth) else Modifier.weight(1f),
                onOpen = { actions.onOpen(index) },
                onSave = { actions.onSave(index) },
            )
        }
        if (files.size > 1) SaveAllTile(actions.onSaveAll)
    }
}

@Composable
private fun FileCard(
    file: AttachmentFile,
    modifier: Modifier,
    onOpen: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier
            .height(QuickViewTokens.tileHeight)
            .clip(TileShape)
            .background(OtusColors.bgRegular)
            .quickClick("${file.name}, ${file.detail}", TileShape, onOpen, onHold = onSave, holdLabel = "Save")
            .padding(start = QuickViewTokens.tileGap, end = QuickViewTokens.fileEnd),
        horizontalArrangement = Arrangement.spacedBy(QuickViewTokens.fileGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileIcon(file, QuickViewTokens.fileIconSize)
        Column(Modifier.weight(1f)) {
            Text(
                file.name,
                color = OtusColors.text1,
                fontSize = QuickViewTokens.fileNameSize,
                lineHeight = QuickViewTokens.fileNameLineHeight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                file.detail,
                color = OtusColors.text3,
                fontSize = QuickViewTokens.labelSize,
                lineHeight = QuickViewTokens.labelLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SaveAllTile(onSaveAll: () -> Unit) {
    Box(
        Modifier
            .width(QuickViewTokens.tileHeight)
            .height(QuickViewTokens.tileHeight)
            .clip(TileShape)
            .background(OtusColors.bgRegular)
            .quickClick("Save all files", TileShape, onSaveAll),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(QuickViewTokens.tileLabelGap),
        ) {
            Symbol(Res.drawable.symbol_download)
            QuickLabel("Save all", OtusColors.text1)
        }
    }
}
