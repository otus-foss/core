package com.otusfoss.core.designsystem.shared.selection.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_close
import com.otusfoss.core.designsystem.resources.symbol_search
import com.otusfoss.core.designsystem.shared.controls.elements.IconButton
import com.otusfoss.core.designsystem.shared.controls.elements.IconButtonLook
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.input.elements.SearchField
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionCurve
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/**
 * A rounded search field for a picker. Its clear button fades and grows in once there is text, and
 * shrinks away when the text is cleared.
 */
@Composable
public fun PickerSearch(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    Surface(
        modifier,
        shape = CircleShape,
        color = OtusColors.bgRegular,
        border = BorderStroke(Sizes.borderWidth, OtusColors.strokeBgDark),
    ) {
        Row(
            Modifier
                .heightIn(min = Sizes.touchTarget)
                .padding(start = SheetTokens.inset, end = Spacing.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Symbol(Res.drawable.symbol_search)
            SearchField(
                value,
                onChange,
                placeholder,
                Modifier.weight(1f).padding(horizontal = SheetTokens.gap),
                onSearch = { focus.clearFocus() },
            )
            val arrive = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Enter)
            val leave = motionTween<Float>(MotionSpeed.Quick, MotionCurve.Exit)
            AnimatedVisibility(
                value.isNotEmpty(),
                enter = fadeIn(arrive) + scaleIn(arrive),
                exit = fadeOut(leave) + scaleOut(leave),
            ) {
                IconButton(
                    "Clear picker search",
                    { onChange("") },
                    look = IconButtonLook.Plain,
                ) { Symbol(Res.drawable.symbol_close) }
            }
        }
    }
}
