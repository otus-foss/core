package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otusfoss.core.designsystem.mail.folders.components.FolderHint
import com.otusfoss.core.designsystem.mail.folders.components.FolderName
import com.otusfoss.core.designsystem.shared.controls.components.StripChevron
import com.otusfoss.core.designsystem.shared.rows.components.StripCard
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens

/**
 * The slim strip of a single message that is in other folders too: "Also in" and the [names] as
 * a [FolderHint], then a chevron. The whole strip is one button that runs [onClick], usually to
 * open the folders sheet.
 */
@Composable
public fun AlsoInStrip(
    names: List<FolderName>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StripCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.slimHeight)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = ReadTokens.slimStart, end = ReadTokens.slimEnd),
            horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) { FolderHint(names, prefix = "Also in") }
            StripChevron(up = true)
        }
    }
}
