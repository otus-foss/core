package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.otus.designsystem.elements.PageDots
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The thread strip above the read view's action bar: the [heading], the avatar of the page's
 * sender with a title such as "Latest" or the sender's name, and under it [PageDots] for the page
 * at [current] of [count] with the [detail], such as "5/6 · 10 Oct". [trailing] holds short folder
 * hints before the chevron. The dots and [detail] always show in full; [trailing] gets the width
 * they leave, and the title gives way to both. A `FirstFit` lets it shorten itself. The whole strip
 * is one button that runs [onClick], usually to grow it into a [ThreadList]; screen readers hear
 * "<title>, message x of n, <detail>".
 */
@Composable
public fun PositionStrip(
    heading: NamedAvatar,
    count: Int,
    current: Int,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val position = StripPosition(count, current, detail)
    PositionCard(position.spoken(heading), onClick, modifier) {
        PositionInfo(heading, position, Modifier.weight(1f), trailing)
    }
}

/**
 * The same strip, which screen readers hear as [description] when it is not null. The caller
 * then names what [trailing] shows too, such as the sender, the date, and the folders.
 */
@Composable
public fun PositionStrip(
    heading: NamedAvatar,
    count: Int,
    current: Int,
    detail: String,
    onClick: () -> Unit,
    description: String?,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val position = StripPosition(count, current, detail)
    PositionCard(description ?: position.spoken(heading), onClick, Modifier) {
        PositionInfo(heading, position, Modifier.weight(1f), trailing)
    }
}

/** Where the page sits, as a [PositionStrip] shows it. */
internal class StripPosition(
    val count: Int,
    val current: Int,
    val detail: String,
) {
    /** What screen readers hear without a description of the caller's: "<title>, message x of n, <detail>". */
    fun spoken(heading: NamedAvatar): String = "${heading.name}, message ${current + 1} of $count, $detail"
}

/** The strip's card: one button that runs [onClick], heard as [spoken], with [content] before the chevron. */
@Composable
internal fun PositionCard(
    spoken: String,
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    StripCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.positionHeight)
                .clickable(role = Role.Button, onClick = onClick)
                .clearAndSetSemantics {
                    contentDescription = spoken
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                }.padding(start = ReadTokens.stripStart, end = ReadTokens.stripEnd),
            horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
            StripChevron(up = true)
        }
    }
}

/** The sender's avatar, then the title over the dots and detail, beside [trailing]. */
@Composable
internal fun PositionInfo(
    heading: NamedAvatar,
    position: StripPosition,
    modifier: Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(heading.avatar, ReadTokens.stripAvatarSize)
        PositionText(heading.name, position, Modifier.weight(1f), trailing)
    }
}

/**
 * The strip's title over its dots and detail, beside the [trailing] slot. The dots line takes
 * its full width first, the slot gets what is left, and the title shares the line's width.
 */
@Composable
private fun PositionText(
    title: String,
    position: StripPosition,
    modifier: Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    Layout(
        listOf(
            {
                Text(
                    title,
                    style = panelTextStyle(),
                    fontSize = ReadTokens.titleSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ReadTokens.dotsGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PageDots(position.count, position.current)
                    Text(
                        position.detail,
                        style = panelTextStyle(),
                        color = OtusColors.text3,
                        fontSize = ReadTokens.detailSize,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            },
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ReadTokens.glyphGap),
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing,
                )
            },
        ),
        modifier,
    ) { (titles, lines, slots), constraints ->
        val width = constraints.maxWidth
        val gap = ReadTokens.stripGap.roundToPx()
        val line = lines.single().measure(Constraints(maxWidth = width))
        val slot = slots.single().measure(Constraints(maxWidth = (width - line.width - gap).coerceAtLeast(0)))
        val textWidth = if (slot.width == 0) width else width - slot.width - gap
        val heading = titles.single().measure(Constraints(maxWidth = textWidth.coerceAtLeast(0)))
        val height = maxOf(heading.height + line.height, slot.height, constraints.minHeight)
        layout(width, height) {
            val top = (height - heading.height - line.height) / 2
            heading.placeRelative(0, top)
            line.placeRelative(0, top + heading.height)
            slot.placeRelative(width - slot.width, (height - slot.height) / 2)
        }
    }
}
