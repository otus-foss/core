package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.controls.components.StripChevron
import com.otusfoss.core.designsystem.shared.people.components.PersonAvatar
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The messages of a conversation as a [PositionStrip] grown into a list, newest first like its
 * dots: a header with the [title], such as "6 messages", that runs [onFold], then one row per
 * entry with its sender, one line of its text, and its date. The [current] row is tinted and its
 * name semibold; tapping a row runs [onSelect] with its index. The rows scroll once the list
 * reaches [maxHeight]. Put it inside a [StripCard][com.otusfoss.core.designsystem.shared.rows.components.StripCard].
 */
@Composable
public fun ThreadList(
    title: String,
    entries: List<ThreadEntry>,
    current: Int,
    onSelect: (Int) -> Unit,
    onFold: () -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp = ReadTokens.threadListMaxHeight,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.listHeaderHeight)
                .clickable(onClickLabel = "Fold", role = Role.Button, onClick = onFold)
                .padding(start = StripTokens.start + StripTokens.gap / 2, end = StripTokens.end),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                Modifier.weight(1f),
                style = panelTextStyle(),
                fontSize = StripTokens.titleSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StripChevron(up = false)
        }
        Column(
            Modifier
                .heightIn(max = (maxHeight - ReadTokens.listHeaderHeight).coerceAtLeast(0.dp))
                .verticalScroll(rememberScrollState()),
        ) {
            entries.forEachIndexed { index, entry ->
                ThreadListRow(entry, index == current) { onSelect(index) }
            }
        }
    }
}

@Composable
private fun ThreadListRow(
    entry: ThreadEntry,
    current: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (current) OtusColors.bgRegular else Color.Transparent)
            .topHairline(OtusColors.strokeBgDark)
            .selectable(current, role = Role.Button, onClick = onClick)
            .heightIn(min = Sizes.touchTarget)
            .padding(start = StripTokens.start, end = StripTokens.start),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(entry.avatar, ReadTokens.listAvatarSize)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal)) {
                    append(entry.name)
                }
                withStyle(SpanStyle(color = OtusColors.text3, fontSize = ReadTokens.previewSize)) {
                    append(PREVIEW_GAP + entry.preview)
                }
            },
            Modifier.weight(1f),
            style = panelTextStyle(),
            fontSize = StripTokens.titleSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            entry.date,
            style = panelTextStyle(),
            color = OtusColors.text3,
            fontSize = ReadTokens.dateSize,
            maxLines = 1,
        )
    }
}

private const val PREVIEW_GAP = "   "
