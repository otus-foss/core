package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.PageDots
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The thread strip above the read view's action bar: the [heading], the avatar of the page's
 * sender with a title such as "Latest" or the sender's name, and under it [PageDots] for the page
 * at [current] of [count] with the [detail], such as "5/6 · 10 Oct". [trailing] holds short folder
 * hints before the chevron; it keeps its natural width and the title gives way. The whole strip
 * is one button that runs [onClick], usually to grow it into a [ThreadList]; screen readers hear
 * "<title>, message x of n".
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
    val description = "${heading.name}, message ${current + 1} of $count"
    StripCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.positionHeight)
                .clickable(role = Role.Button, onClick = onClick)
                .clearAndSetSemantics {
                    contentDescription = description
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                }.padding(start = ReadTokens.stripStart, end = ReadTokens.stripEnd),
            horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonAvatar(heading.avatar, ReadTokens.stripAvatarSize)
            Column(Modifier.weight(1f)) {
                Text(
                    heading.name,
                    style = panelTextStyle(),
                    fontSize = ReadTokens.titleSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ReadTokens.dotsGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PageDots(count, current)
                    Text(
                        detail,
                        style = panelTextStyle(),
                        color = OtusColors.text3,
                        fontSize = ReadTokens.detailSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing()
            StripChevron(up = true)
        }
    }
}
