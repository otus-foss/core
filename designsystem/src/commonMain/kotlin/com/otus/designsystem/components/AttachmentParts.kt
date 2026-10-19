package com.otus.designsystem.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_download
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/** Up to [ReadTokens.THUMB_LIMIT] small thumbnails that overlap, each ringed in the strip's color. */
@Composable
internal fun ThumbStack(files: List<AttachmentFile>) {
    Row(horizontalArrangement = Arrangement.spacedBy(-ReadTokens.thumbOverlap)) {
        files.take(ReadTokens.THUMB_LIMIT).forEach { file ->
            val shape = RoundedCornerShape(ReadTokens.thumbRadius)
            Box(Modifier.background(OtusColors.bgDark, shape).padding(ReadTokens.thumbRing)) {
                FileTile(
                    file,
                    ReadTokens.thumbSize - ReadTokens.thumbRing * 2,
                    ReadTokens.thumbRadius - ReadTokens.thumbRing,
                    ReadTokens.thumbIconSize,
                )
            }
        }
    }
}

/** A file's thumbnail, or its kind's symbol on a `bgRegular` tile with a `bgLight` border. */
@Composable
internal fun FileTile(
    file: AttachmentFile,
    size: Dp,
    radius: Dp,
    iconSize: Dp,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(OtusColors.bgRegular)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape),
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
        FileTile(file, ReadTokens.fileTileSize, ReadTokens.fileTileRadius, PanelTokens.iconSize)
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
        IconButton("Save ${file.name}", onSave, outlined = false) {
            CompositionLocalProvider(
                LocalContentColor provides OtusColors.text2,
            ) { Symbol(Res.drawable.symbol_download) }
        }
    }
}

private const val SUMMARY_SPLIT = " · "
