package com.otus.designsystem.mail.attachments.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_download
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.rows.components.topHairline
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Up to [ReadTokens.THUMB_LIMIT] small round thumbnails that overlap, each ringed in the strip's
 * color, as a stack of avatars is.
 */
@Composable
internal fun ThumbStack(files: List<AttachmentFile>) {
    Row(horizontalArrangement = Arrangement.spacedBy(-ReadTokens.thumbOverlap)) {
        files.take(ReadTokens.THUMB_LIMIT).forEach { file ->
            Box(Modifier.background(OtusColors.bgDark, CircleShape).padding(ReadTokens.thumbRing)) {
                FileTile(file, ReadTokens.thumbSize - ReadTokens.thumbRing * 2, ReadTokens.thumbIconSize)
            }
        }
    }
}

/**
 * A file's thumbnail, or its kind's symbol, on a round `bgRegular` tile with a `bgLight` border,
 * round like the avatars and buttons beside it.
 */
@Composable
internal fun FileTile(
    file: AttachmentFile,
    size: Dp,
    iconSize: Dp,
) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(OtusColors.bgRegular)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val thumbnail = file.thumbnail
        if (thumbnail != null) {
            Image(thumbnail, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            CompositionLocalProvider(
                LocalContentColor provides OtusColors.text2,
            ) { Symbol(file.kind.icon, size = iconSize) }
        }
    }
}

/** The summary, such as "3 attachments · 4.2 MB", with the part after the first dot in `text3`. */
@Composable
internal fun SummaryText(
    summary: String,
    fontSize: TextUnit,
    bold: Boolean,
    modifier: Modifier = Modifier,
) {
    val head = summary.substringBefore(SUMMARY_SPLIT)
    val tail = summary.removePrefix(head)
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)) { append(head) }
            withStyle(SpanStyle(color = OtusColors.text3)) { append(tail) }
        },
        modifier,
        style = panelTextStyle(),
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** One file in the opened strip: tapping it runs [onOpen]; its download button runs [onSave]. */
@Composable
internal fun FileRow(
    file: AttachmentFile,
    onOpen: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = ReadTokens.fileRowHeight)
            .topHairline(OtusColors.strokeBgDark)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = ReadTokens.stripStart + ReadTokens.stripGap / 2, end = ReadTokens.stripEnd / 2),
        horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileTile(file, ReadTokens.fileTileSize, PanelTokens.iconSize)
        Column(Modifier.weight(1f)) {
            Text(
                file.name,
                style = panelTextStyle(),
                fontSize = ReadTokens.titleSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                file.detail,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // The default ripple, as on Save all, also shows a quick tap inside the scrolling file list.
        Box(
            Modifier
                .size(PanelTokens.touchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onSave)
                .semantics { contentDescription = "Save ${file.name}" },
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides OtusColors.text2,
            ) { Symbol(Res.drawable.symbol_download) }
        }
    }
}

private const val SUMMARY_SPLIT = " · "
