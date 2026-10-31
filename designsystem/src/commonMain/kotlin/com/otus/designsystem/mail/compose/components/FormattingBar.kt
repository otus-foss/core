package com.otus.designsystem.mail.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.otus.designsystem.mail.compose.elements.RichText
import com.otus.designsystem.mail.compose.elements.TextFormat
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_format_bold
import com.otus.designsystem.resources.symbol_format_clear
import com.otus.designsystem.resources.symbol_format_italic
import com.otus.designsystem.resources.symbol_format_list_bulleted
import com.otus.designsystem.resources.symbol_format_list_numbered
import com.otus.designsystem.resources.symbol_link
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.shared.actionbar.components.LocalBarParts
import com.otus.designsystem.shared.actionbar.components.overKeyboard
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.panels.components.drawEdgeFade
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.editing.ComposeTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * The row docked above the keyboard while formatted [text] is being written. [leading], such as
 * Attach, comes first behind a divider, then a button for each [TextFormat]; the row scrolls
 * sideways and fades at its edges when it does not fit. Link runs [onLink] so the caller can ask
 * for an address, or removes the link at the cursor. [trailing], such as a [CompactSendButton],
 * stays pinned at the end, flush with the row's edge and drawn over its border. The row spans
 * the width above the keyboard; see [overKeyboard]. Inside a
 * [BarMorph][com.otus.designsystem.shared.actionbar.components.BarMorph], the row and [trailing]
 * move into the other bar's parts.
 */
@Composable
public fun FormattingBar(
    text: RichText,
    onLink: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = SmoothCornerShape(ComposeTokens.formatBarRadius)
    val parts = LocalBarParts.current
    Box(
        // Widen before applying BarMorph so it moves the whole row.
        Modifier
            .overKeyboard()
            .then(parts.group)
            .then(modifier)
            .fillMaxWidth()
            .height(ComposeTokens.formatBarHeight)
            .background(OtusColors.bgDark, shape),
    ) {
        // Drawn before the buttons, so the Send's accent border covers it where the two meet.
        Box(Modifier.matchParentSize().border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape))
        FormattingRow(text, onLink, leading, trailing?.let { { Box(parts.primary) { it() } } })
    }
}

@Composable
private fun FormattingRow(
    text: RichText,
    onLink: () -> Unit,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
) {
    Row(
        Modifier
            .fillMaxSize()
            .padding(
                start = ComposeTokens.formatBarInset,
                end = if (trailing == null) ComposeTokens.formatBarInset else 0.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val scroll = rememberScrollState()
        Row(
            Modifier.weight(1f).scrollFade(scroll).horizontalScroll(scroll),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                LeadingDivider()
            }
            TextFormat.entries.forEach { format ->
                FormatButton(format.label, format.symbol, text.isActive(format), { text.applyFormat(format, onLink) })
            }
        }
        trailing?.invoke()
    }
}

/** The line between [FormattingBar]'s leading button and the formats. */
@Composable
private fun LeadingDivider() {
    Box(
        Modifier
            .padding(horizontal = PanelTokens.iconGap)
            .size(PanelTokens.borderWidth, ComposeTokens.formatDividerHeight)
            .background(OtusColors.bgLight),
    )
}

/** Toggles [format], except that adding a link runs [onLink] so the caller can ask for an address. */
private fun RichText.applyFormat(
    format: TextFormat,
    onLink: () -> Unit,
) {
    if (format == TextFormat.Link && !isActive(format)) onLink() else toggle(format)
}

/** One button of a [FormattingBar]; an [active] format is filled with `selectedFill`. */
@Composable
public fun FormatButton(
    description: String,
    icon: DrawableResource,
    active: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(ComposeTokens.formatButtonRadius)
    Box(
        Modifier
            .size(PanelTokens.touchTarget)
            .semantics {
                contentDescription = description
                selected = active
            }.clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.size(ComposeTokens.formatButtonSize).controlPressFeedback(interactions, shape),
            shape = shape,
            color = if (active) OtusColors.selectedFill else OtusColors.bgDark,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
        ) { Box(contentAlignment = Alignment.Center) { Symbol(icon, size = ComposeTokens.formatIconSize) } }
    }
}

/**
 * The Send at the end of a [FormattingBar]: the [icon], the send symbol by default, on the accent.
 * It is a circle as tall as the row, so its end lines up with the row's rounded end.
 */
@Composable
public fun CompactSendButton(
    description: String,
    onClick: () -> Unit,
    enabled: Boolean,
    icon: DrawableResource = Res.drawable.symbol_send,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = CircleShape
    Box(
        Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .semantics { contentDescription = description }
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.fillMaxSize().controlPressFeedback(interactions, shape),
            shape = shape,
            color = if (enabled) OtusColors.accentDark else OtusColors.bgDark,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
            border =
                BorderStroke(
                    PanelTokens.borderWidth,
                    if (enabled) OtusColors.accentRegular else OtusColors.strokeBgDark,
                ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Symbol(icon, mirrorInRtl = true, size = ActionBarTokens.symbolSize)
            }
        }
    }
}

private fun Modifier.scrollFade(scroll: ScrollState): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val fade = PanelTokens.fadeWidth.toPx().coerceAtMost(size.width / 2)
        drawEdgeFade((scroll.value / fade).coerceIn(0f, 1f), fade, atEnd = false)
        drawEdgeFade(((scroll.maxValue - scroll.value) / fade).coerceIn(0f, 1f), fade, atEnd = true)
    }

private val TextFormat.label: String
    get() =
        when (this) {
            TextFormat.Bold -> "Bold"
            TextFormat.Italic -> "Italic"
            TextFormat.Bullets -> "Bulleted list"
            TextFormat.Numbers -> "Numbered list"
            TextFormat.Link -> "Link"
            TextFormat.Clear -> "Clear formatting"
        }

private val TextFormat.symbol: DrawableResource
    get() =
        when (this) {
            TextFormat.Bold -> Res.drawable.symbol_format_bold
            TextFormat.Italic -> Res.drawable.symbol_format_italic
            TextFormat.Bullets -> Res.drawable.symbol_format_list_bulleted
            TextFormat.Numbers -> Res.drawable.symbol_format_list_numbered
            TextFormat.Link -> Res.drawable.symbol_link
            TextFormat.Clear -> Res.drawable.symbol_format_clear
        }
