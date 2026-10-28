package com.otus.designsystem.shared.controls.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.settings.SettingTokens

/**
 * An on/off switch. It only shows [checked]; the enclosing row is the toggle and supplies the
 * accessible name and switch role.
 */
@Composable
public fun ToggleSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val travel = SettingTokens.switchWidth - SettingTokens.switchThumb - SettingTokens.switchInset * 2
    val thumb by animateDpAsState(if (checked) travel else 0.dp, motionTween(MotionSpeed.Fast))
    val track by animateColorAsState(
        if (checked) OtusColors.accentLight else OtusColors.bgLight,
        motionTween(MotionSpeed.Fast),
    )
    Box(
        modifier
            .size(SettingTokens.switchWidth, SettingTokens.switchHeight)
            .clip(CircleShape)
            .background(track)
            .padding(SettingTokens.switchInset),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = thumb)
                .size(SettingTokens.switchThumb)
                .clip(CircleShape)
                .background(OtusColors.text1),
        )
    }
}
