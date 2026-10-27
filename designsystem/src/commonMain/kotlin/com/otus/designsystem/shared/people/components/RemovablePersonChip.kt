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
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A picked person in a filter, such as a sender on a
 * [PeopleLine][com.otus.designsystem.shared.filters.components.PeopleLine]: a `selectedFill` capsule with
 * the [person]'s avatar, name, and a ✕ that calls [onRemove]. Only the ✕ reacts to taps, so a tap
 * on the rest reaches the line; screen readers hear it as "Remove Grace Hopper".
 */
@Composable
public fun RemovablePersonChip(
    person: Recipient,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.height(FilterTokens.chipHeight),
        shape = CircleShape,
        color = OtusColors.selectedFill,
        contentColor = OtusColors.text1,
        border = BorderStroke(PanelTokens.borderWidth, OtusColors.selectedFill),
    ) {
        Row(
            Modifier.padding(start = FilterTokens.chipIconGap),
            horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.clearAndSetSemantics {}) { PersonAvatar(person.avatar, FilterTokens.chipAvatarSize) }
            Text(
                person.name,
                Modifier.widthIn(max = FilterTokens.personChipNameMaxWidth).clearAndSetSemantics {},
                style = panelTextStyle(),
                fontSize = FilterTokens.chipTextSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Compose widens this small target to 48 dp for touch, without making the chip taller.
            Box(
                Modifier
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onRemove)
                    .semantics { contentDescription = "Remove ${person.name}" }
                    .padding(start = PanelTokens.iconGap, end = FilterTokens.chipStartWithIcon),
                contentAlignment = Alignment.Center,
            ) { Symbol(Res.drawable.symbol_close, size = FilterTokens.chipIconSize) }
        }
    }
}
