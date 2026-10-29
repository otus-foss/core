package com.otus.designsystem.shared.reorder.components

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_drag_indicator
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/** One row of a [ReorderableColumn] or [ReorderableTree]: its drag gestures and its Move up and Move down actions. */
internal class ReorderRow(
    private val drag: ReorderDrag,
    private val key: Any,
    private val haptics: HapticFeedback,
    private val up: (() -> Unit)?,
    private val down: (() -> Unit)?,
) : ReorderableScope {
    @Composable
    override fun DragHandle(label: String) {
        Box(
            Modifier
                .size(PanelTokens.touchTarget, PanelTokens.compactRowHeight)
                .semantics {
                    contentDescription = "Reorder $label"
                    customActions = moveActions()
                }.pointerInput(drag, key) {
                    detectVerticalDragGestures(
                        onDragStart = { drag.start(key) },
                        onDragEnd = drag::drop,
                        onDragCancel = drag::cancel,
                    ) { change, amount ->
                        change.consume()
                        drag.moveBy(amount)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_drag_indicator)
            }
        }
    }

    override fun Modifier.dragOnLongPress(): Modifier =
        semantics { customActions = moveActions() }
            .pointerInput(drag, key) {
                detectLongPressDrag(
                    onStart = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        drag.start(key)
                    },
                    onDrag = { drag.moveBy(it.y) },
                    onEnd = drag::drop,
                    onCancel = drag::cancel,
                )
            }

    private fun moveActions(): List<CustomAccessibilityAction> =
        listOfNotNull(moveAction("Move up", up), moveAction("Move down", down))
}

private fun moveAction(
    label: String,
    move: (() -> Unit)?,
): CustomAccessibilityAction? =
    move?.let { run ->
        CustomAccessibilityAction(label) {
            run()
            true
        }
    }
