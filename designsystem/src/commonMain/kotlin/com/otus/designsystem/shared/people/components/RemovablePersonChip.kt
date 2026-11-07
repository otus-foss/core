package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.controls.ControlTokens
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A picked person in a filter, such as a sender on a
 * [PeopleLine][com.otus.designsystem.shared.filters.components.PeopleLine]: a `selectedFill` capsule with
 * the [person]'s avatar, name, and a ✕ that calls [onRemove]. Only the ✕ reacts to taps, so a tap
 * on the rest reaches the line; screen readers hear it as "Remove Grace Hopper".
 */
@Composable
internal fun RemovablePersonChip(
    person: Recipient,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.height(Sizes.chipHeight),
        shape = CircleShape,
        color = OtusColors.selectedFill,
        contentColor = OtusColors.text1,
        border = BorderStroke(Sizes.borderWidth, OtusColors.selectedFill),
    ) {
        Row(
            Modifier.padding(start = ControlTokens.chipIconGap),
            horizontalArrangement = Arrangement.spacedBy(ControlTokens.chipIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.clearAndSetSemantics {}) { PersonAvatar(person.avatar, ControlTokens.chipAvatarSize) }
            Text(
                person.name,
                Modifier.widthIn(max = ControlTokens.chipNameMaxWidth).clearAndSetSemantics {},
                style = panelTextStyle(),
                fontSize = TypeScale.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Compose widens this small target to 48 dp for touch, without making the chip taller.
            Box(
                Modifier
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onRemove)
                    .semantics { contentDescription = "Remove ${person.name}" }
                    .padding(start = Spacing.iconGap, end = ControlTokens.chipPadding),
                contentAlignment = Alignment.Center,
            ) { Symbol(Res.drawable.symbol_close, size = ControlTokens.chipIconSize) }
        }
    }
}
