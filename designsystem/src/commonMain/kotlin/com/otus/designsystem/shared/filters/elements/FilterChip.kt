package com.otus.designsystem.shared.filters.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.controls.elements.reportControlVisualBounds
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.selection.FilterTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/** A [FilterChip] with a plain [label]. */
@Composable
public fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: FilterChipRole = FilterChipRole.Checkbox,
    leading: (@Composable () -> Unit)? = null,
) {
    FilterChip(ChipLabel(label), selected, onClick, modifier, role, leading)
}

/**
 * A choice in a filter panel section: a 36 dp capsule in a 48 dp touch target, `bgRegular` while
 * off and `selectedFill` while [selected]. An optional 18 dp [leading] icon or avatar comes first.
 * The [label]'s prefix, such as the parent folders of a folder whose name repeats, shows in `text3`.
 */
@Composable
public fun FilterChip(
    label: ChipLabel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: FilterChipRole = FilterChipRole.Checkbox,
    leading: (@Composable () -> Unit)? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val on = selected && role != FilterChipRole.Button
    val choose =
        when (role) {
            FilterChipRole.Checkbox -> {
                Modifier.toggleable(on, interactions, null, role = Role.Checkbox) { onClick() }
            }

            FilterChipRole.Radio -> {
                Modifier.selectable(
                    on,
                    interactions,
                    null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
            }

            FilterChipRole.Button -> {
                Modifier.clickable(interactions, null, role = Role.Button, onClick = onClick)
            }
        }
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = label.spoken }
            .then(choose),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.reportControlVisualBounds().controlPressFeedback(interactions),
            shape = CircleShape,
            color = if (on) OtusColors.selectedFill else OtusColors.bgRegular,
            contentColor = OtusColors.text1,
            border = BorderStroke(PanelTokens.borderWidth, if (on) OtusColors.selectedFill else OtusColors.bgLight),
        ) {
            Row(
                Modifier
                    .height(FilterTokens.chipHeight)
                    .padding(
                        start = if (leading == null) FilterTokens.chipStart else FilterTokens.chipStartWithIcon,
                        end = FilterTokens.chipEnd,
                    ).clearAndSetSemantics {},
                horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipIconGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke()
                FilterChipText(label)
            }
        }
    }
}

@Composable
private fun FilterChipText(label: ChipLabel) {
    val faint = OtusColors.text3
    val text =
        buildAnnotatedString {
            label.prefix?.let { withStyle(SpanStyle(color = faint)) { append("$it / ") } }
            append(label.text)
        }
    Text(
        text,
        style = panelTextStyle().copy(color = Color.Unspecified, fontSize = FilterTokens.chipTextSize),
        maxLines = 1,
    )
}
