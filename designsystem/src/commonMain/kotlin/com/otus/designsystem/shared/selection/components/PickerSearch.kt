package com.otus.designsystem.shared.selection.components

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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.input.elements.SearchField
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

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
        border = BorderStroke(PanelTokens.borderWidth, OtusColors.strokeBgDark),
    ) {
        Row(
            Modifier
                .heightIn(min = PanelTokens.touchTarget)
                .padding(start = SheetTokens.inset, end = PanelTokens.iconGap),
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
