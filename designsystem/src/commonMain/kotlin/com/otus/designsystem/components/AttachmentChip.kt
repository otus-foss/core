package com.otus.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.IconButtonLook
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_attach_file
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A file attached to a message being written: its [name], and a [detail] under it such as its
 * size or "Uploading…". Tapping the chip opens [actions] in a [ContextMenu] under it, such as
 * Retry or Show in message, when there are any; ✕ runs [onRemove].
 */
@Composable
public fun AttachmentChip(
    name: String,
    detail: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<MenuAction> = emptyList(),
) {
    val shape = RoundedCornerShape(ComposeTokens.recipientRadius)
    var open by remember { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(0) }
    Row(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .widthIn(max = ComposeTokens.attachmentMaxWidth)
            .onSizeChanged { height = it.height }
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .then(
                if (actions.isEmpty()) {
                    Modifier
                } else {
                    Modifier.clickable(onClickLabel = "More options", role = Role.Button) { open = true }
                },
            ).semantics(mergeDescendants = true) { contentDescription = "$name, $detail" }
            .padding(start = PanelTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_attach_file)
        }
        Column(Modifier.weight(1f, fill = false)) {
            Text(name, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                detail,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ComposeTokens.detailSize,
                maxLines = 1,
            )
        }
        IconButton("Remove $name", onRemove, look = IconButtonLook.Plain) {
            Symbol(Res.drawable.symbol_close, size = PanelTokens.iconSize)
        }
        if (open) ContextMenu(actions, Offset(0f, height.toFloat())) { open = false }
    }
}
