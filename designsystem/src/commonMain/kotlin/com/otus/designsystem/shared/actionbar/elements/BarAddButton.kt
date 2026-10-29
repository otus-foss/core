package com.otus.designsystem.shared.actionbar.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_add
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.settings.elements.outlineDash
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * An empty bar slot in a 48 dp target: a dashed circle with a plus, shaped like
 * [AddSwatch][com.otus.designsystem.shared.settings.elements.AddSwatch] so it reads as room for
 * another action rather than as one. [label] names it for screen readers.
 */
@Composable
public fun BarAddButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val outline = OtusColors.text3
    Box(
        modifier
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = label }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(ActionBarTokens.addSlotSize).drawBehind {
                val width = PanelTokens.borderWidth.toPx()
                drawCircle(
                    outline,
                    radius = (size.minDimension - width) / 2,
                    style = Stroke(width, pathEffect = outlineDash()),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_add, size = ActionBarTokens.addSymbolSize)
            }
        }
    }
}
