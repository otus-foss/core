package com.otus.designsystem.shared.folders.elements

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_folder_open
import com.otus.designsystem.resources.symbol_folder_open_fill
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.LocalIconTint
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder icon in a target 48 dp wide and one compact row tall. With a non-null [expanded], it
 * folds the subfolders of the folder named [label] and shows an open folder while expanded.
 * Otherwise it shows [closed], such as a shared folder's icon. The icon takes [LocalIconTint], and
 * a tinted folder is filled.
 */
@Composable
internal fun FolderToggle(
    label: String,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    closed: DrawableResource = Res.drawable.symbol_folder,
) {
    val interactions = remember { MutableInteractionSource() }
    val toggle =
        expanded?.let {
            Modifier
                .semantics { contentDescription = if (it) "Collapse $label" else "Expand $label" }
                .toggleable(it, interactions, indication = null, role = Role.Button, onValueChange = onExpandedChange)
        } ?: Modifier
    Box(
        Modifier.size(Sizes.touchTarget, Sizes.compactRowHeight).then(toggle),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(SidebarTokens.expandButton).controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) { LeadingSymbol(folderSymbol(expanded == true, closed, filled = LocalIconTint.current != null)) }
    }
}

private fun folderSymbol(
    open: Boolean,
    closed: DrawableResource,
    filled: Boolean,
): DrawableResource =
    when {
        open && filled -> Res.drawable.symbol_folder_open_fill
        open -> Res.drawable.symbol_folder_open
        else -> closed
    }
