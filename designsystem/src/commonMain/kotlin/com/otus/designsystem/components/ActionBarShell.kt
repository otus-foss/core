package com.otus.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Transition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.elements.textEnterSpec
import com.otus.designsystem.elements.textExitSpec
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween
import kotlin.math.roundToInt

/**
 * The primary action in its shell. The shell is one button in both states, so its description
 * names the context too. The action's end edge and icon stay put while its width animates. A bar
 * that appears with a context starts full and collapses into it, like one whose context arrives later.
 * When the primary changes, its icon and label swap inside the shell; see [rememberFrontBar].
 */
@Composable
internal fun PrimaryShell(
    primary: BarAction,
    context: BarContext?,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    val interactions = remember { MutableInteractionSource() }
    // The last context stays drawn while it fades out.
    val last = remember { LastContext() }
    if (context != null) last.value = context
    val shown = last.value
    val animation = remember { Animatable(0f) }
    val collapse = motionTween<Float>(MotionSpeed.Fast)
    LaunchedEffect(context != null) { animation.animateTo(if (context == null) 0f else 1f, collapse) }
    val progress = animation.value
    val fadeStart = ActionBarTokens.CONTEXT_FADE_START
    val textColor = if (primary.enabled) OtusColors.text1 else OtusColors.text3
    val face = rememberFrontBar(primary, context).face
    Box(
        modifier
            .heightIn(min = ActionBarTokens.height)
            .background(OtusColors.bgDark, shape)
            .semantics { contentDescription = listOfNotNull(primary.label, context?.description).joinToString(", ") }
            .clickable(interactions, null, enabled = primary.enabled, role = Role.Button, onClick = primary.onClick)
            .controlPressFeedback(interactions, shape),
    ) {
        // Drawn before the action, so the action's accent border covers it where the two meet.
        Box(Modifier.matchParentSize().border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape))
        CompositionLocalProvider(LocalContentColor provides textColor) {
            shown?.let {
                ContextContent(
                    primary.label,
                    it,
                    if (primary.enabled) OtusColors.text2 else OtusColors.text3,
                    Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer { alpha = ((progress - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f) }
                        .padding(
                            start = ActionBarTokens.contextInset,
                            end = ActionBarTokens.collapsedActionWidth + ActionBarTokens.tokenGap,
                        ),
                )
            }
            Action(primary, face, shape, Modifier.matchParentSize()) { progress }
            face.Swapping(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = ActionBarTokens.labelInset, end = ActionBarTokens.collapsedActionWidth)
                    .graphicsLayer { alpha = (1f - progress / fadeStart).coerceIn(0f, 1f) }
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    it.label,
                    fontSize = ActionBarTokens.titleSize,
                    lineHeight = ActionBarTokens.titleLineHeight,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** The filled action, [progress] of the way from the full shell to its collapsed width. */
@Composable
private fun Action(
    primary: BarAction,
    face: Transition<PrimaryFace>,
    shape: RoundedCornerShape,
    modifier: Modifier,
    progress: () -> Float,
) {
    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .collapsingWidth(progress)
                .background(if (primary.enabled) OtusColors.accentDark else OtusColors.bgDark, shape)
                // A disabled action loses its accent entirely, so it reads as part of the shell.
                .border(
                    PanelTokens.borderWidth,
                    if (primary.enabled) OtusColors.accentRegular else OtusColors.strokeBgDark,
                    shape,
                ),
        ) {
            Box(
                Modifier.align(Alignment.CenterEnd).width(ActionBarTokens.collapsedActionWidth).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) { face.Swapping { Symbol(it.symbol, it.mirrorInRtl, ActionBarTokens.symbolSize) } }
        }
    }
}

/** Measures between the full available width and the collapsed action width. */
private fun Modifier.collapsingWidth(progress: () -> Float): Modifier =
    layout { measurable, constraints ->
        val full = constraints.maxWidth
        val collapsed = ActionBarTokens.collapsedActionWidth.roundToPx().coerceAtMost(full)
        val width = (full + (collapsed - full) * progress()).roundToInt()
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

@Composable
internal fun ContextContent(
    title: String,
    context: BarContext,
    subtitleColor: Color,
    modifier: Modifier,
) {
    Row(
        modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.tokenGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val motionScale = LocalMotionScale.current
        AnimatedContent(context.token, contentKey = {
            it.key
        }, transitionSpec = { crossfade(motionScale) }, label = "Bar token") {
            BarTokenView(it)
        }
        Column {
            FadingText(title, ActionBarTokens.titleSize, FontWeight.SemiBold, LocalContentColor.current)
            FadingText(context.subtitle, ActionBarTokens.subtitleSize, FontWeight.Normal, subtitleColor)
        }
    }
}

/** Crossfades between texts with the [com.otus.designsystem.elements.AnimatedText] timings. */
@Composable
private fun FadingText(
    text: String,
    size: TextUnit,
    weight: FontWeight,
    color: Color,
) {
    val motionScale = LocalMotionScale.current
    AnimatedContent(text, transitionSpec = { crossfade(motionScale) }, label = "Bar text") {
        Text(it, color = color, fontSize = size, fontWeight = weight, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun crossfade(motionScale: Float) =
    fadeIn(textEnterSpec(motionScale)) togetherWith fadeOut(textExitSpec(motionScale))

/**
 * The last context shown, kept while the context fades out. It is not state, because it changes
 * only during a recomposition.
 */
internal class LastContext {
    var value: BarContext? = null
}
