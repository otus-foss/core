package com.otusfoss.core.designsystem.shared.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.controls.elements.TextLink
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarOwl
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.onboarding.OnboardingTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/**
 * The first or last step inside an [OnboardingFrame]: the owl, a large [title] over an optional [body], then
 * [content], such as what the user chose, centered in the room above the buttons. [primary] is
 * the one accent button at the bottom, with [secondary] below it, then [link] as underlined text.
 */
@Composable
public fun ColumnScope.OnboardingHeroBody(
    title: String,
    primary: UiAction,
    body: String? = null,
    secondary: UiAction? = null,
    link: UiAction? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(OnboardingTokens.heroGap, Alignment.CenterVertically),
    ) {
        HeroText(title, body)
        content()
    }
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActionButton(primary.label, primary.onClick, Modifier.fillMaxWidth(), primary = true, enabled = primary.enabled)
        secondary?.let { ActionButton(it.label, it.onClick, Modifier.fillMaxWidth(), enabled = it.enabled) }
        link?.let { TextLink(it.label, it.onClick) }
    }
}

/** The owl, then the title and the body, inset like free-standing page text. */
@Composable
private fun HeroText(
    title: String,
    body: String?,
) {
    Column(
        Modifier.padding(horizontal = OnboardingTokens.heroInset - PageTokens.pageInset),
        verticalArrangement = Arrangement.spacedBy(OnboardingTokens.heroGap),
    ) {
        SidebarOwl(Modifier.height(OnboardingTokens.owlHeight))
        Text(
            title,
            Modifier.semantics { heading() },
            fontSize = OnboardingTokens.heroTitleSize,
            lineHeight = OnboardingTokens.heroTitleLineHeight,
            fontWeight = FontWeight.SemiBold,
        )
        if (body != null) {
            Text(body, color = OtusColors.text2, fontSize = TypeScale.body, lineHeight = TypeScale.titleLineHeight)
        }
    }
}
