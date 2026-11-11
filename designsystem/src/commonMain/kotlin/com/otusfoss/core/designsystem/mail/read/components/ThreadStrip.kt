package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_unfold_more
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.people.components.AvatarStack
import com.otusfoss.core.designsystem.shared.people.components.PersonAvatar
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.content.ThreadTokens
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The earlier messages of the thread a reply answers, just above its action bar. Up to
 * [ThreadTokens.ROW_LIMIT] show as rows with their sender and date; more fold into one row with
 * their avatars, the [summary], such as "5 earlier messages", and its [detail], such as who wrote
 * the latest. Tapping anywhere runs [onOpen].
 */
@Composable
public fun ThreadStrip(
    entries: List<ThreadEntry>,
    summary: String,
    detail: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = SmoothCornerShape(ActionBarTokens.radius)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OtusColors.bgDark)
            .strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, shape)
            .clickable(onClickLabel = "Show the thread", role = Role.Button, onClick = onOpen),
    ) {
        if (entries.size > ThreadTokens.ROW_LIMIT) {
            ThreadSummary(entries, summary, detail)
        } else {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(Sizes.borderWidth).background(OtusColors.bgLight))
                }
                ThreadRow(entry)
            }
        }
    }
}

@Composable
private fun ThreadRow(entry: ThreadEntry) {
    StripLine(ThreadTokens.rowHeight) {
        PersonAvatar(entry.avatar, ThreadTokens.avatarSize)
        Text(
            entry.name,
            Modifier.weight(1f),
            style = panelTextStyle(),
            fontSize = ThreadTokens.nameSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(entry.date, style = panelTextStyle(), color = OtusColors.text3, fontSize = ThreadTokens.detailSize)
    }
}

@Composable
private fun ThreadSummary(
    entries: List<ThreadEntry>,
    summary: String,
    detail: String,
) {
    StripLine(ThreadTokens.summaryHeight) {
        AvatarStack(entries.take(ThreadTokens.ROW_LIMIT).map { it.avatar }, ring = OtusColors.bgDark)
        Column(Modifier.weight(1f)) {
            Text(
                summary,
                style = panelTextStyle(),
                fontSize = ThreadTokens.nameSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                detail,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ThreadTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_unfold_more)
        }
    }
}

/** One line of the strip, at least [minHeight] tall, its parts spaced like the action bar's context. */
@Composable
private fun StripLine(
    minHeight: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(horizontal = ActionBarTokens.contextInset),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.contextInset),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** A message in the thread sheet: avatar, sender, date, and one line of its text. */
@Composable
public fun ThreadMessageRow(
    entry: ThreadEntry,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(
            horizontal = ActionBarTokens.contextInset,
            vertical = ControlTokens.chipPadding,
        ),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.contextInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(entry.avatar, ThreadTokens.sheetAvatarSize)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.name,
                    Modifier.weight(1f),
                    style = panelTextStyle(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(entry.date, style = panelTextStyle(), color = OtusColors.text3, fontSize = ThreadTokens.detailSize)
            }
            Text(
                entry.preview,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = ThreadTokens.nameSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
