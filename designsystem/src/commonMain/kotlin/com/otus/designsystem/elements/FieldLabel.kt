package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The small `text2` label above a form field, such as "First name", the same gap above every
 * field. An [action] at its end, such as an info button, overhangs the line instead of making it
 * taller, so a label with one sits as close to its field as one without.
 */
@Composable
public fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().padding(horizontal = SheetTokens.gap), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            Modifier.weight(1f),
            style = panelTextStyle(),
            fontSize = SheetTokens.detailSize,
            color = OtusColors.text2,
        )
        action?.let {
            Box(
                Modifier.layout { measurable, constraints ->
                    val placeable =
                        measurable.measure(
                            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity),
                        )
                    layout(placeable.width, 0) { placeable.place(0, -placeable.height / 2) }
                },
            ) { it() }
        }
    }
}

/** The ✕ inside a form field that removes the field, read to screen readers as [description]. */
@Composable
public fun RemoveFieldButton(
    description: String,
    onRemove: () -> Unit,
) {
    IconButton(description, onRemove, look = IconButtonLook.Bare) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_close) }
    }
}
