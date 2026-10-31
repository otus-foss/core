package com.otus.designsystem.shared.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.shared.controls.elements.ButtonPill
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.rows.InboxTokens
import org.jetbrains.compose.resources.DrawableResource

/** Centered, scrollable empty content with an optional action. */
@Composable
public fun EmptyState(
    title: String,
    description: String?,
    icon: DrawableResource,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().padding(InboxTokens.contentInset), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(InboxTokens.itemGap),
        ) {
            Symbol(icon, size = InboxTokens.emptyIconSize)
            Text(title, fontSize = InboxTokens.subjectSize, textAlign = TextAlign.Center)
            if (description != null) Text(description, color = OtusColors.text2, textAlign = TextAlign.Center)
            if (actionLabel != null) ButtonPill(actionLabel, onAction, fill = OtusColors.bgLight)
        }
    }
}
