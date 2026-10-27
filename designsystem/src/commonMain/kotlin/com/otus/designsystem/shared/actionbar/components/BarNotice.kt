package com.otus.designsystem.shared.actionbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A strip just above a page's action bar that says what the bar's main action will do differently,
 * such as "Scheduled for Mon 19 Oct, 8:00": an [icon], the [text], and an [actionLabel] button
 * that runs [onAction], such as Change.
 */
@Composable
public fun BarNotice(
    icon: DrawableResource,
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    Row(
        modifier
            .fillMaxWidth()
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(start = ActionBarTokens.contextInset, end = buttonInset),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.tokenGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon)
        Text(
            text,
            Modifier.weight(1f),
            style = panelTextStyle(),
            fontSize = ActionBarTokens.subtitleSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        NoticeButton(actionLabel, onAction)
    }
}

/** A filled text button inside a full touch target, at the notice's end. */
@Composable
private fun NoticeButton(
    label: String,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    // Concentric with the notice: its corners, less the gap around the button.
    val shape = RoundedCornerShape(ActionBarTokens.radius - buttonInset)
    Box(
        Modifier
            .heightIn(min = PanelTokens.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .heightIn(min = PanelTokens.chipHeight)
                .background(OtusColors.bgRegular, shape)
                .controlPressFeedback(interactions, shape)
                .padding(horizontal = PanelTokens.chipPadding + PanelTokens.iconGap),
            contentAlignment = Alignment.Center,
        ) { Text(label, style = panelTextStyle(), fontSize = ActionBarTokens.subtitleSize) }
    }
}

/**
 * The gap around the notice's button on every side: its pill sits centered in a 48 dp touch area
 * that fills the notice's height, so the end padding matches the space above and below.
 */
private val buttonInset = (PanelTokens.touchTarget - PanelTokens.chipHeight) / 2
