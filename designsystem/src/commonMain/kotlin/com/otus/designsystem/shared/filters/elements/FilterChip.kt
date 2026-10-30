package com.otus.designsystem.shared.filters.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.otus.designsystem.shared.controls.elements.chipAction
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
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = label.spoken }
            .chipChoice(role, on, interactions, onClick),
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

/** Makes the chip a checkbox, a radio button, or a plain button, as [role] says. */
private fun Modifier.chipChoice(
    role: FilterChipRole,
    on: Boolean,
    interactions: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier =
    when (role) {
        FilterChipRole.Checkbox -> chipAction(interactions, on, enabled = true, onClick)
        FilterChipRole.Radio -> radioChoice(on, interactions, onClick)
        FilterChipRole.Button -> chipAction(interactions, selected = null, enabled = true, onClick)
    }

@Composable
private fun FilterChipText(label: ChipLabel) {
    Text(
        label.annotated(),
        style = panelTextStyle().copy(color = Color.Unspecified, fontSize = FilterTokens.chipTextSize),
        maxLines = 1,
    )
}

/** The label's text after its `text3` prefix and a slash, such as "Freelancing / Invoices". */
@Composable
internal fun ChipLabel.annotated(): AnnotatedString {
    val faint = OtusColors.text3
    val label = this
    return buildAnnotatedString {
        val prefix = label.prefix
        if (prefix != null) withStyle(SpanStyle(color = faint)) { append("$prefix / ") }
        append(label.text)
    }
}

private fun Modifier.radioChoice(
    selected: Boolean,
    source: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = selectable(selected, source, indication = null, role = Role.RadioButton, onClick = onClick)
