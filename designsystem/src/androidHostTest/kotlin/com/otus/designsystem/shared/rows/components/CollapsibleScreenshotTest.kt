package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.pages.components.pageScreenshot
import com.otus.designsystem.shared.sidebar.elements.SidebarExpandButton
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CollapsibleScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private class Folder(
        val name: String,
        val depth: Int,
    )

    private val folders =
        listOf(
            Folder("Inbox", 0),
            Folder("Projects", 0),
            Folder("Otus", 1),
            Folder("Design", 2),
            Folder("Receipts", 1),
            Folder("Archive", 0),
        )

    @Test
    fun expanded() = compose.pageScreenshot("collapsible_expanded") { Nested(expanded = true) }

    @Test
    fun collapsed() = compose.pageScreenshot("collapsible_collapsed") { Nested(expanded = false) }

    @Test
    fun treeExpanded() = compose.pageScreenshot("collapsible_tree_expanded") { Tree() }

    @Test
    fun treeWithCollapsedParent() = compose.pageScreenshot("collapsible_tree_collapsed") { Tree(collapse = "Projects") }

    @Test
    fun treeExpandAll() = compose.pageScreenshot("collapsible_tree_expand_all") { Tree(expandAll = true) }

    @Composable
    private fun Nested(expanded: Boolean) {
        Column {
            Text("Projects")
            Collapsible(expanded) {
                Column {
                    Text("Otus")
                    Text("Receipts")
                }
            }
            Text("Archive")
        }
    }

    /** The tree keeps its collapsed set private, so [collapse] folds that folder the way a tap would. */
    @Composable
    private fun Tree(
        collapse: String? = null,
        expandAll: Boolean = false,
    ) {
        CollapsibleTree(
            folders,
            { it.name },
            { it.depth },
            expandAll = expandAll,
        ) { folder, expanded, onExpandedChange ->
            if (folder.name == collapse) LaunchedEffect(Unit) { onExpandedChange(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    DetailRow(folder.name, emptyList(), {}, depth = folder.depth) { Symbol(Res.drawable.symbol_folder) }
                }
                expanded?.let { SidebarExpandButton(folder.name, it, onExpandedChange) }
            }
        }
    }
}
