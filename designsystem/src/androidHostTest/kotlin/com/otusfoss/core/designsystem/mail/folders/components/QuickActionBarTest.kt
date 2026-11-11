package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_delete
import com.otusfoss.core.designsystem.resources.symbol_edit
import com.otusfoss.core.designsystem.shared.actionbar.components.QuickActionBar
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarFolder
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QuickActionBarTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aLongPressOpensTheBarAndAnActionRunsAndClosesIt() {
        val chosen = mutableListOf<String>()
        var clicks = 0
        compose.setContent {
            var at by remember { mutableStateOf<Offset?>(null) }
            FolderSettingRow(
                SidebarFolder("Receipts"),
                expanded = null,
                onExpandedChange = {},
                onClick = { clicks++ },
                onLongClick = { at = it },
            )
            val point = at
            if (point != null) {
                val actions =
                    listOf(
                        UiAction("Rename", Res.drawable.symbol_edit) { chosen += "Rename" },
                        UiAction("Delete", Res.drawable.symbol_delete) { chosen += "Delete" },
                    )
                QuickActionBar(actions, point, onDismiss = { at = null })
            }
        }
        compose.onNodeWithText("Receipts").performTouchInput { longClick() }
        compose.onNodeWithContentDescription("Delete").performClick()
        assertEquals(listOf("Delete"), chosen)
        assertEquals(0, clicks, "A long press does not also open the folder")
        compose.onNodeWithContentDescription("Rename").assertDoesNotExist()
    }
}
