package com.otus.settings.presentation.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.otus.designsystem.theme.OtusTheme
import com.otus.settings.presentation.screens.SettingsScreen
import com.otus.settings.presentation.settingsIndex
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w412dp-h892dp-xxhdpi")
class SettingsScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsRendersOnlyTheEntriesAndCallbacksSuppliedByTheApp() {
        var opened = 0
        var navigation = 0
        compose.setContent {
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
            ) {
                OtusTheme {
                    SettingsScreen(
                        settingsIndex { page("Preferences", { opened++ }, summary = "Choose your defaults") },
                        onOpenNavigation = { navigation++ },
                    )
                }
            }
        }
        compose.onNodeWithText("Choose your defaults").assertIsDisplayed()
        compose.onNodeWithText("Email accounts").assertDoesNotExist()
        compose.onNodeWithText("Folders").assertDoesNotExist()
        compose.onNodeWithText("Preferences").performClick()
        compose.onNodeWithContentDescription("Open navigation").performClick()
        compose.runOnIdle {
            assertEquals(1, opened)
            assertEquals(1, navigation)
        }
    }
}
