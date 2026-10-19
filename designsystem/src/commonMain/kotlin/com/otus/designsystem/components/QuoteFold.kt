package com.otus.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_unfold_less
import com.otus.designsystem.resources.symbol_unfold_more
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.motionTween
import com.otus.designsystem.theme.panelTextStyle

/**
 * The quoted part of a reply, folded under its body. A `bgLight` line runs along its start; the
 * header shows the quoted [person] and the [date] in one pill, divided by a line, which runs
 * [onPerson] when given; without a person, the date alone.
 * Folded, the first lines of [text] fade out; "Show quoted text" runs [onToggle] to unfold it
 * while [expanded] is false. A quote short enough to show whole has no fade and no toggle.
 */
@Composable
public fun QuoteFold(
    person: NamedAvatar?,
    date: String,
    text: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPerson: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var overflows by remember(text) { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .startBorder()
                .padding(start = ReadTokens.quoteInset)
                .animateContentSize(motionTween(MotionSpeed.Normal)),
            verticalArrangement = Arrangement.spacedBy(ReadTokens.quoteHeaderGap),
        ) {
            QuoteHeader(person, date, onPerson)
            Text(
                text,
                if (!expanded && overflows) Modifier.fadeOut() else Modifier,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.quoteTextSize,
                lineHeight = ReadTokens.quoteLineHeight,
                maxLines = if (expanded) Int.MAX_VALUE else ReadTokens.QUOTE_FOLDED_LINES,
                onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
            )
        }
        if (expanded || overflows) QuoteToggle(expanded, onToggle)
    }
}

@Composable
private fun QuoteHeader(
    person: NamedAvatar?,
    date: String,
    onPerson: (() -> Unit)?,
) {
    if (person != null) {
        PersonPill(
            person.name,
            person.avatar,
            PersonPillSize.Small,
            onClick = onPerson,
            detail = date.ifBlank { null },
        )
    } else {
        Text(
            date,
            style = panelTextStyle(),
            color = OtusColors.text3,
            fontSize = ReadTokens.quoteTextSize,
            maxLines = 1,
        )
    }
}

@Composable
private fun QuoteToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        Modifier
            .heightIn(min = PanelTokens.touchTarget)
            .clickable(role = Role.Button, onClick = onToggle),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(
                if (expanded) Res.drawable.symbol_unfold_less else Res.drawable.symbol_unfold_more,
                size = ReadTokens.quoteToggleIcon,
            )
        }
        Text(
            if (expanded) "Hide quoted text" else "Show quoted text",
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = ReadTokens.quoteTextSize,
        )
    }
}

/** The quote's `bgLight` line along its start edge. */
@Composable
private fun Modifier.startBorder(): Modifier {
    val color = OtusColors.bgLight
    return drawBehind {
        val x = ReadTokens.quoteBorder.toPx() / 2
        drawLine(color, Offset(x, 0f), Offset(x, size.height), ReadTokens.quoteBorder.toPx())
    }
}

/** Fades the content out toward its bottom edge, by masking it with a gradient. */
@Composable
private fun Modifier.fadeOut(): Modifier {
    val opaque = OtusColors.text1
    return graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
        drawContent()
        drawRect(
            Brush.verticalGradient(
                ReadTokens.QUOTE_FADE_START to opaque,
                1f to opaque.copy(alpha = 0f),
            ),
            blendMode = BlendMode.DstIn,
        )
    }
}
