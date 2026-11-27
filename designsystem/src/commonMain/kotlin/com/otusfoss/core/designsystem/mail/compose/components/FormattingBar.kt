package com.otusfoss.core.designsystem.mail.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.getValue
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
import com.otusfoss.core.designsystem.mail.compose.elements.RichText
import com.otusfoss.core.designsystem.mail.compose.elements.TextFormat
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_send
import com.otusfoss.core.designsystem.shared.actionbar.components.LocalBarParts
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.panels.components.drawEdgeFade
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.editing.ComposeTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.ActionBarTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * The row docked above the keyboard while formatted [text] is being written. [leading], such as
 * Attach, stays put at the start behind a divider. A button for each [TextFormat] follows; these
 * scroll sideways and fade at the divider and the end when they do not fit. While words are
 * selected, [leading] and the formats that do not change the selection step aside. Link runs
 * [onLink] so the caller can ask for an address, or removes the link at the cursor. [trailing],
 * such as a [CompactSendButton], stays pinned at the end, flush with the row's edge and drawn over
 * its border. Inside a
 * [BarMorph][com.otusfoss.core.designsystem.shared.actionbar.components.BarMorph], the row and [trailing]
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
    val shape = SmoothCornerShape(ActionBarTokens.formatBarRadius)
    val parts = LocalBarParts.current
    Box(
        parts.group
            .then(modifier)
            .fillMaxWidth()
            .height(ComposeTokens.formatBarHeight)
            .background(OtusColors.bgDark, shape),
    ) {
        // Drawn before the buttons, so the Send's accent border covers it where the two meet.
        Box(Modifier.matchParentSize().strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, shape))
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
        val selecting = text.selectedText.isNotEmpty()
        if (leading != null) {
            Shown(!selecting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leading()
                    LeadingDivider()
                }
            }
        }
        val gap by animateDpAsState(
            if (leading == null || selecting) 0.dp else Spacing.iconGap,
            motionTween(MotionSpeed.Quick),
            label = "Format gap",
        )
        val scroll = rememberScrollState()
        Row(
            Modifier
                .weight(1f)
                .scrollFade(scroll)
                .horizontalScroll(scroll)
                .padding(start = gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shown = text.shownFormats()
            val activeAt = { index: Int -> shown.getOrNull(index)?.let(text::isActive) == true }
            TextFormat.entries.forEach { format ->
                val index = shown.indexOf(format)
                Shown(index >= 0) {
                    FormatCell(
                        format.label,
                        format.symbol,
                        activeAt(index),
                        onClick = { text.applyFormat(format, onLink) },
                        joinsStart = activeAt(index) && activeAt(index - 1),
                        joinsEnd = activeAt(index) && activeAt(index + 1),
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

/**
 * The line between [FormattingBar]'s leading button and the formats. The formats scroll and fade
 * out at this line, so it has no gap on its end side; the scrolling row pads its content instead.
 */
@Composable
private fun LeadingDivider() {
    Box(
        Modifier
            .padding(start = Spacing.iconGap)
            .size(Sizes.borderWidth, ComposeTokens.formatDividerHeight)
            .background(OtusColors.bgLight),
    )
}

/**
 * Every format at a caret. While words are selected, only the ones that change those words: Bold,
 * Italic and Link, plus Clear when one of them is on. Lists work on whole lines either way.
 */
private fun RichText.shownFormats(): List<TextFormat> =
    if (selectedText.isEmpty()) {
        TextFormat.entries
    } else {
        listOf(TextFormat.Bold, TextFormat.Italic, TextFormat.Link)
            .let { inline -> if (inline.any(::isActive)) inline + TextFormat.Clear else inline }
    }

/** [content] that grows in and fades in when [visible], and shrinks away when not. */
@Composable
private fun RowScope.Shown(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible,
        enter = fadeIn(motionTween(MotionSpeed.Quick)) + expandHorizontally(motionTween(MotionSpeed.Quick)),
        exit = fadeOut(motionTween(MotionSpeed.Quick)) + shrinkHorizontally(motionTween(MotionSpeed.Quick)),
    ) { content() }
}

/** Toggles [format], except that adding a link runs [onLink] so the caller can ask for an address. */
private fun RichText.applyFormat(
    format: TextFormat,
    onLink: () -> Unit,
) {
    if (format == TextFormat.Link && !isActive(format)) onLink() else toggle(format)
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
                    Sizes.borderWidth,
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
        val fade = Sizes.edgeFade.toPx().coerceAtMost(size.width / 2)
        drawEdgeFade((scroll.value / fade).coerceIn(0f, 1f), fade, atEnd = false)
        drawEdgeFade(((scroll.maxValue - scroll.value) / fade).coerceIn(0f, 1f), fade, atEnd = true)
    }
