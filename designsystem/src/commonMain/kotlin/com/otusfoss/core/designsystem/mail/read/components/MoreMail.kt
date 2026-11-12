package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.controls.components.StripChevron
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * One mail in a [MoreMail] or [RecentMail] list: its [subject], the start of its text, its [date],
 * whether it is [unread], and whether it is the [current] one, such as the thread the user reads.
 */
public class MailLine(
    public val subject: String,
    public val preview: String,
    public val date: String,
    public val unread: Boolean = false,
    public val current: Boolean = false,
)

/**
 * Other mail that belongs with the open message, such as more from its sender, as a section that
 * sits straight on a panel: a header with the [title] that runs [onToggle], and, while [open], one
 * row per mail. An unread mail's subject is semibold. Tapping a row runs [onOpen] with its index.
 */
@Composable
public fun MoreMail(
    title: String,
    lines: List<MailLine>,
    open: Boolean,
    onToggle: () -> Unit,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.attachmentHeight)
                .topHairline(OtusColors.strokeBgDark)
                .clickable(onClickLabel = if (open) "Fold" else "Unfold", role = Role.Button, onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                Modifier.weight(1f),
                style = panelTextStyle(),
                fontSize = StripTokens.titleSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StripChevron(up = open)
        }
        if (open) lines.forEachIndexed { index, line -> MailLineRow(line) { onOpen(index) } }
    }
}

/** One [line]; the [current] one sits on the selected fill, its text [inset] from the fill's edges. */
@Composable
internal fun MailLineRow(
    line: MailLine,
    inset: Dp = 0.dp,
    onClick: () -> Unit,
) {
    val shape = SmoothCornerShape(CornerTokens.row)
    val detail = if (line.current) OtusColors.text2 else OtusColors.text3
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = ReadTokens.fileRowHeight)
            .clip(shape)
            .background(if (line.current) OtusColors.selectedFill else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = inset),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                line.subject,
                style = panelTextStyle(),
                fontSize = StripTokens.titleSize,
                fontWeight = if (line.unread) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                line.preview,
                style = panelTextStyle(),
                color = detail,
                fontSize = ReadTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            line.date,
            style = panelTextStyle(),
            color = detail,
            fontSize = ReadTokens.detailSize,
            maxLines = 1,
        )
    }
}
