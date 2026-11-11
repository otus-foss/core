package com.otusfoss.core.designsystem.shared.sidebar.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.SidebarTokens

/** A hairline between groups of sidebar rows, such as the mailboxes and the folders. */
@Composable
public fun SidebarDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = SidebarTokens.entryPadding, vertical = SidebarTokens.inset)
            .fillMaxWidth()
            .height(Sizes.borderWidth)
            .background(OtusColors.bgLight),
    )
}
