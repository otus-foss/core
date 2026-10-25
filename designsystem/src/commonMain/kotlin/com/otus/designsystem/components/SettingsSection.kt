package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SettingTokens

/**
 * A group of [SettingRow]s under a large [title]; rows follow each other without gaps. An
 * [avatar], such as the account the rows belong to, sits before the title.
 */
@Composable
public fun SettingsSection(
    title: String,
    avatar: ChipAvatar? = null,
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
            avatar?.let {
                PersonAvatar(it, SettingTokens.sectionAvatar)
            }
            Text(
                title,
                fontSize = SettingTokens.sectionTitleSize,
                lineHeight = SettingTokens.sectionTitleLineHeight,
                fontWeight = FontWeight.SemiBold,
            )
        }
        content()
    }
}
