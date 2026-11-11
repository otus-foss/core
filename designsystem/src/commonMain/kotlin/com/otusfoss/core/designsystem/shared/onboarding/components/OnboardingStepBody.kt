package com.otusfoss.core.designsystem.shared.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.onboarding.OnboardingTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/**
 * One step inside an [OnboardingFrame]: a large [title] over an optional [body], the scrolling
 * [content], and the [footer] with the step's button, which stays at the bottom.
 */
@Composable
public fun ColumnScope.OnboardingStepBody(
    title: String,
    body: String? = null,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
    ) {
        StepTitle(title, body)
        content()
    }
    Column(
        Modifier.fillMaxWidth().padding(top = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = footer,
    )
}

/** The step's title, then what it asks for in `text2`, inset like free-standing page text. */
@Composable
private fun StepTitle(
    title: String,
    body: String?,
) {
    Column(
        Modifier.padding(horizontal = PageTokens.textInset).padding(bottom = PageTokens.rowGap),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
    ) {
        Text(
            title,
            Modifier.semantics { heading() },
            fontSize = OnboardingTokens.stepTitleSize,
            lineHeight = OnboardingTokens.stepTitleLineHeight,
            fontWeight = FontWeight.SemiBold,
        )
        if (body != null) {
            Text(body, color = OtusColors.text2, fontSize = TypeScale.body, lineHeight = TypeScale.titleLineHeight)
        }
    }
}
