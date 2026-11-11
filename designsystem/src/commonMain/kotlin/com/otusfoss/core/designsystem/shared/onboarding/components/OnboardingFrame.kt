package com.otusfoss.core.designsystem.shared.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_left
import com.otusfoss.core.designsystem.shared.controls.elements.IconButton
import com.otusfoss.core.designsystem.shared.controls.elements.IconButtonLook
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The screen every step of a first run shares, without the app's control panel: one top row with
 * the back chevron's slot and the [progress], a step's place and how many there are, then the
 * current step as [content], an [OnboardingStepBody] or an [OnboardingHeroBody]. The row stays on
 * screen from step to step, so the progress animates between them and never moves. The chevron and
 * the system back both run [onBack]; a step without one keeps the chevron's slot empty.
 */
@Composable
public fun OnboardingFrame(
    progress: Pair<Int, Int>,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = onBack != null,
        onBackCompleted = { onBack?.invoke() },
    )
    ProvideTextStyle(panelTextStyle()) {
        Column(
            modifier
                .fillMaxSize()
                .background(OtusColors.bgRegular)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(PageTokens.pageInset),
        ) {
            Row(Modifier.height(Sizes.touchTarget), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(Sizes.touchTarget), contentAlignment = Alignment.Center) {
                    if (onBack != null) {
                        IconButton("Back", onBack, look = IconButtonLook.Plain) {
                            Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true)
                        }
                    }
                }
                StepProgress(progress.first, progress.second, Modifier.weight(1f))
            }
            content()
        }
    }
}
