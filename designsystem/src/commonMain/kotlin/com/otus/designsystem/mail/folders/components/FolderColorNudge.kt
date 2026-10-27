package com.otus.designsystem.mail.folders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.resources.symbol_palette
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.NudgeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

/**
 * A card above a list that offers a color for a folder that has none. [title] and [body] read as
 * one paragraph, with the title in SemiBold text1. Each of [colors] is a filled circle the user
 * can tap; [onDismiss] runs from the close button.
 */
@Composable
public fun FolderColorNudge(
    title: String,
    body: String,
    colors: List<FolderColor>,
    onPick: (FolderColor) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(NudgeTokens.radius)
    Column(
        modifier
            .fillMaxWidth()
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(horizontal = NudgeTokens.paddingHorizontal, vertical = NudgeTokens.paddingVertical),
        verticalArrangement = Arrangement.spacedBy(NudgeTokens.rowGap),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(NudgeTokens.iconGap)) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_palette, size = NudgeTokens.iconSize)
            }
            NudgeText(title, body, Modifier.weight(1f))
            CloseButton(onDismiss)
        }
        Row(
            Modifier.padding(start = NudgeTokens.iconSize + NudgeTokens.iconGap),
            horizontalArrangement = Arrangement.spacedBy(NudgeTokens.swatchGap),
        ) { colors.forEach { ColorChoice(it) { onPick(it) } } }
    }
}

@Composable
private fun NudgeText(
    title: String,
    body: String,
    modifier: Modifier,
) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = OtusColors.text1, fontWeight = FontWeight.SemiBold)) { append(title) }
            append(body)
        },
        modifier,
        color = OtusColors.text2,
        fontSize = NudgeTokens.textSize,
        lineHeight = NudgeTokens.lineHeight,
    )
}

/**
 * The 48 dp target overhangs the card's padding without taking layout space, so the icon lines up
 * with the first line of text.
 */
@Composable
private fun CloseButton(onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    Box(Modifier.size(NudgeTokens.iconSize).wrapContentSize(unbounded = true)) {
        Box(
            Modifier
                .size(PanelTokens.touchTarget)
                .semantics { contentDescription = "Dismiss" }
                .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
                .controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
                Symbol(Res.drawable.symbol_close, size = NudgeTokens.iconSize)
            }
        }
    }
}

@Composable
private fun ColorChoice(
    color: FolderColor,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(NudgeTokens.swatchSize)
            .background(color.tint ?: Color.Transparent, CircleShape)
            .semantics { contentDescription = color.label }
            .clickable(role = Role.Button, onClick = onClick),
    )
}
