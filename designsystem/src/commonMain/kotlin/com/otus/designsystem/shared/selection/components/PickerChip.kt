package com.otus.designsystem.shared.selection.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.shared.controls.elements.ChipFrame
import com.otus.designsystem.shared.controls.elements.ChipLayout
import com.otus.designsystem.shared.controls.elements.ChipText
import com.otus.designsystem.shared.controls.elements.ChipWidth
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.people.components.AvatarStack
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.theme.colors.OtusColors

/**
 * A pill with the current choice, its [avatars] and [label], and a down chevron, such as which
 * account a page shows or which address a draft is sent from. It calls [onClick], which opens a
 * picker; screen readers hear it as one button named [label]. Given a width, such as by
 * [ContextPanelScope.fillRail][com.otus.designsystem.shared.panels.components.ContextPanelScope.fillRail], it fills
 * it and puts the chevron at the end.
 */
@Composable
public fun PickerChip(
    avatars: List<ChipAvatar>,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipFrame(
        onClick,
        modifier.clearAndSetSemantics {
            contentDescription = label
            role = Role.Button
            onClick {
                onClick()
                true
            }
        },
        selected = false,
        enabled = true,
        fill = OtusColors.bgDark,
        layout = ChipLayout(ChipWidth.Fill),
    ) {
        AvatarStack(avatars, ring = OtusColors.bgLight)
        ChipText(label)
        Symbol(Res.drawable.symbol_expand_more)
    }
}
