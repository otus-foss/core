package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SettingTokens

/** A group of [SettingRow]s under a large [title]; rows follow each other without gaps. */
@Composable
public fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.padding(top = SettingTokens.sectionGap)) {
        Text(
            title,
            Modifier
                .padding(horizontal = PageTokens.textInset)
                .padding(bottom = SettingTokens.sectionTitleGap)
                .semantics { heading() },
            fontSize = SettingTokens.sectionTitleSize,
            lineHeight = SettingTokens.sectionTitleLineHeight,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}
