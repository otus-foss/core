package com.otus.designsystem.mail.quickview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens

/**
 * A quick view module for one person, such as who sent the open message: their [avatar], [name],
 * [address], and a [detail] line, such as how the user knows them. A tap runs [onClick], such as
 * opening what the app offers for them.
 */
@Composable
public fun QuickPerson(
    avatar: ChipAvatar,
    name: String,
    address: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuickModule(modifier, Arrangement.Start) {
        Row(
            Modifier
                .weight(1f)
                .height(QuickViewTokens.tileHeight)
                .clip(TileShape)
                .background(OtusColors.bgRegular)
                .quickClick("$name, $address. $detail", TileShape, onClick)
                .padding(start = QuickViewTokens.personStart, end = QuickViewTokens.personEnd),
            horizontalArrangement = Arrangement.spacedBy(QuickViewTokens.personGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonAvatar(avatar, QuickViewTokens.faceSize)
            Column(Modifier.weight(1f)) {
                Line(name, OtusColors.text1, FontWeight.SemiBold, QuickViewTokens.personNameSize)
                Line(address, OtusColors.text2, FontWeight.Normal, QuickViewTokens.personAddressSize)
                Line(detail, OtusColors.text3, FontWeight.Normal, QuickViewTokens.labelSize)
            }
            CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
                Symbol(Res.drawable.symbol_chevron_right)
            }
        }
    }
}

@Composable
private fun Line(
    text: String,
    color: Color,
    weight: FontWeight,
    size: TextUnit,
) {
    Text(text, color = color, fontWeight = weight, fontSize = size, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
