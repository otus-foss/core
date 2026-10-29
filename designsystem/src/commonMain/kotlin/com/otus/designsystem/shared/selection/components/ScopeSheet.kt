package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A [SheetFrame] that picks what a page shows, such as every account, one account, or one
 * address. [search], such as a [PickerSearch], stays above the scrolling [content] of [ScopeRow]s,
 * [ScopeChildRow]s, and [com.otus.designsystem.shared.rows.elements.SectionCaption]s, and [footnote] explains
 * the choice in `text2` at the end. The caller applies a choice at once and owns what is chosen,
 * pinned, and expanded; Done, the scrim, Back, or dragging the sheet down closes it.
 */
@Composable
public fun ScopeSheet(
    title: String,
    onDismiss: () -> Unit,
    search: (@Composable () -> Unit)? = null,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SheetFrame(title, onDismiss) {
        Column(
            Modifier
                .weight(1f, fill = false)
                .padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        ) {
            search?.invoke()
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .selectableGroup(),
            ) {
                content()
                if (footnote != null) {
                    Text(
                        footnote,
                        Modifier.padding(horizontal = SidebarTokens.entryPadding, vertical = SheetTokens.gap),
                        color = OtusColors.text2,
                        fontSize = SheetTokens.detailSize,
                    )
                }
            }
        }
    }
}
