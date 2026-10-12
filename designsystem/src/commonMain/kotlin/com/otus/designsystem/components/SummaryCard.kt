package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.rememberAvatarImage
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.SummaryTokens

/**
 * What a sheet acts on, such as the message being moved: [leading], often an avatar, beside
 * [title] over a `text2` [subtitle], one line each, on a raised `bgRegular` card. It only shows
 * them, so screen readers read it as one item.
 */
@Composable
public fun SummaryCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
) {
    SummaryCardFrame(title, modifier, leading) { SummaryText(subtitle, Modifier) }
}

/**
 * A [SummaryCard] whose second line names [people], such as the senders of several messages,
 * each after a small avatar. Up to three are named; the rest are counted as "+N". Long names
 * shorten so every named person keeps a share of the line.
 */
@Composable
public fun SummaryCard(
    title: String,
    people: List<NamedAvatar>,
    modifier: Modifier = Modifier,
) {
    SummaryCardFrame(title, modifier, leading = {}) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SummaryTokens.personGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            people.take(SummaryTokens.PEOPLE_LIMIT).forEach { person ->
                Row(
                    Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(SummaryTokens.avatarGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(
                        person.avatar.initials,
                        image = rememberAvatarImage(person.avatar.imageBytes),
                        colorIndex = person.avatar.colorIndex,
                        size = SummaryTokens.avatarSize,
                    )
                    SummaryText(person.name, Modifier.weight(1f, fill = false))
                }
            }
            val hidden = people.size - SummaryTokens.PEOPLE_LIMIT
            if (hidden > 0) SummaryText("+$hidden", Modifier)
        }
    }
}

@Composable
private fun SummaryCardFrame(
    title: String,
    modifier: Modifier,
    leading: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SheetTokens.rowRadius))
            .background(OtusColors.bgRegular)
            .semantics(mergeDescendants = true) {}
            .padding(SheetTokens.gap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Column(Modifier.weight(1f)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle()
        }
    }
}

@Composable
private fun SummaryText(
    text: String,
    modifier: Modifier,
) {
    Text(
        text,
        modifier,
        color = OtusColors.text2,
        fontSize = SheetTokens.detailSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
