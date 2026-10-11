package com.otus.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.figtree
import com.otus.designsystem.resources.figtree_black
import com.otus.designsystem.resources.figtree_bold
import com.otus.designsystem.resources.figtree_semibold
import org.jetbrains.compose.resources.Font

/**
 * Figtree in every weight the suite uses, one static file per weight. A single variable file
 * cannot serve several on Android, which caches asset fonts by path and would draw every weight
 * from whichever variation loaded first. Bold and Black are what Normal and SemiBold become when
 * the system's bold text setting adds 300 to every weight; without them both would fall back to
 * SemiBold and unread text would look like the rest.
 */
@Composable
public fun panelTextStyle(): TextStyle {
    val regular = Font(Res.font.figtree, FontWeight.Normal)
    val semiBold = Font(Res.font.figtree_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.figtree_bold, FontWeight.Bold)
    val black = Font(Res.font.figtree_black, FontWeight.Black)
    return remember(regular, semiBold, bold, black) {
        TextStyle(
            fontFamily = FontFamily(regular, semiBold, bold, black),
            fontSize = PanelTokens.textSize,
            lineHeight = PanelTokens.lineHeight,
            color = OtusColors.text1,
        )
    }
}
