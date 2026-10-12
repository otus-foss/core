package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens

/**
 * A `text2` heading followed by a `bgLight` line to the row's end, splitting one list into groups,
 * such as rows that drag between them.
 */
@Composable
public fun DividerLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = SettingTokens.textGap),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, Modifier.semantics { heading() }, color = OtusColors.text2, fontSize = SheetTokens.detailSize)
        Box(Modifier.weight(1f).height(SettingTokens.dividerWidth).background(OtusColors.bgLight))
    }
}
