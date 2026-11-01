package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/** Closing a panel sheet with a swipe up: far enough closes it, else it springs back. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PanelSheetTest {
    @get:Rule
    val compose = createComposeRule()
    private var dismissed = 0

    private fun show() {
        compose.setContent {
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
            ) {
                PanelSheet(
                    open = true,
                    collapsedHeight = 0.dp,
                    onDismiss = { dismissed++ },
                    header = { Text("Filters") },
                ) { Box(Modifier.fillMaxWidth().height(BODY).testTag(BODY_TAG)) }
            }
        }
        compose.waitForIdle()
    }

    private fun swipeUp(
        distance: Dp,
        millis: Long,
    ) {
        compose.onNodeWithTag(BODY_TAG).performTouchInput {
            val travel = distance.toPx() + viewConfiguration.touchSlop
            swipe(center, center - Offset(0f, travel), millis)
        }
        compose.waitForIdle()
    }

    private fun bodyTop() = compose.onNodeWithTag(BODY_TAG).getBoundsInRoot().top

    @Test
    fun aShortSlowSwipeSpringsBack() {
        show()
        val top = bodyTop()
        swipeUp(PanelTokens.sheetCloseSwipe / 2, SLOW_MILLIS)
        assertEquals(0, dismissed)
        assertEquals(top, bodyTop())
    }

    @Test
    fun aSwipePastTheCloseDistanceCloses() {
        show()
        swipeUp(PanelTokens.sheetCloseSwipe * 2, SLOW_MILLIS)
        assertEquals(1, dismissed)
    }

    private companion object {
        val BODY = 200.dp
        const val BODY_TAG = "body"
        const val SLOW_MILLIS = 1_500L
    }
}
