package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_close
import com.otusfoss.core.designsystem.resources.symbol_palette
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.darkSurface
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.feedback.NudgeTokens

/**
 * A card above a list that offers a color for a folder that has none. [title] is a line of its
 * own in SemiBold text1, with [body] below it. Each of [colors] is a filled circle the user
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
    val shape = SmoothCornerShape(NudgeTokens.radius)
    Column(
        modifier
            .fillMaxWidth()
            .darkSurface(shape)
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
    Column(modifier) {
        Text(
            title,
            color = OtusColors.text1,
            fontSize = NudgeTokens.textSize,
            lineHeight = NudgeTokens.lineHeight,
            fontWeight = FontWeight.SemiBold,
        )
        Text(body, color = OtusColors.text2, fontSize = NudgeTokens.textSize, lineHeight = NudgeTokens.lineHeight)
    }
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
                .size(Sizes.touchTarget)
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
