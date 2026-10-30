package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.shared.feedback.components.InfoText
import com.otus.designsystem.shared.feedback.components.InfoTip
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens

/**
 * A group of [SettingRow]s under a large [title]; rows follow each other without gaps. An
 * [avatar], such as the account the rows belong to, sits before the title, and an [info] button
 * after it explains the section on request.
 */
@Composable
public fun SettingsSection(
    title: String,
    avatar: ChipAvatar? = null,
    info: InfoText? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.padding(top = SettingTokens.sectionGap)) {
        Row(
            Modifier
                .padding(horizontal = PageTokens.textInset)
                .padding(bottom = SettingTokens.sectionTitleGap)
                .semantics(mergeDescendants = true) { heading() },
            horizontalArrangement = Arrangement.spacedBy(SettingTokens.sectionLeadingGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (avatar != null) PersonAvatar(avatar, SettingTokens.sectionAvatar)
            Text(
                title,
                fontSize = SettingTokens.sectionTitleSize,
                lineHeight = SettingTokens.sectionTitleLineHeight,
                fontWeight = FontWeight.SemiBold,
            )
            // The button's touch target reaches past the title's line rather than making the header taller.
            if (info != null) {
                InfoTip(info, Modifier.offset(x = -InfoTipOverhang).overlapHeight(PanelTokens.iconSize))
            }
        }
        content()
    }
}

/** How far the info button's touch target reaches past its question mark toward the title. */
private val InfoTipOverhang = (PanelTokens.touchTarget - PanelTokens.iconSize) / 2
