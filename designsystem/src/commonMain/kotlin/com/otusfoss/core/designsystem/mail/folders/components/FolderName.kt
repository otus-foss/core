package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_folder
import com.otusfoss.core.designsystem.shared.folders.elements.FolderGlyph
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder as a [FolderHint] names it: its [path] as the caller wants it shown, its [color], and
 * whether it is a [system] folder such as Archive, which shows an outlined [icon] in `text3`.
 */
public data class FolderName(
    val path: List<String>,
    val color: FolderColor?,
    val system: Boolean,
    val icon: DrawableResource? = null,
)

/** The glyph of [folder] at [size]: tinted when it has a color, an outlined icon when it is a system folder. */
@Composable
internal fun FolderNameGlyph(
    folder: FolderName,
    size: Dp,
) {
    FolderGlyph(
        if (folder.system) null else folder.color,
        icon = folder.icon ?: Res.drawable.symbol_folder,
        size = size,
    )
}
