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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.IconButton
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
 * size or "Uploading…". Tapping the chip runs [onRetry] when there is one, for a failed upload;
 * ✕ runs [onRemove].
 */
@Composable
public fun AttachmentChip(
    name: String,
    detail: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(ComposeTokens.recipientRadius)
    Row(
        modifier
            .heightIn(min = PanelTokens.touchTarget)
            .widthIn(max = ComposeTokens.attachmentMaxWidth)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .then(
                onRetry?.let {
                    Modifier.clickable(onClickLabel = "Retry", role = Role.Button, onClick = it)
                } ?: Modifier,
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
        IconButton("Remove $name", onRemove, outlined = false) {
            Symbol(Res.drawable.symbol_close, size = PanelTokens.iconSize)
        }
    }
}
