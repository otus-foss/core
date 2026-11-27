package com.otusfoss.core.designsystem.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.figtree
import com.otusfoss.core.designsystem.resources.figtree_black
import com.otusfoss.core.designsystem.resources.figtree_bold
import com.otusfoss.core.designsystem.resources.figtree_round
import com.otusfoss.core.designsystem.resources.figtree_round_black
import com.otusfoss.core.designsystem.resources.figtree_round_bold
import com.otusfoss.core.designsystem.resources.figtree_round_semibold
import com.otusfoss.core.designsystem.resources.figtree_semibold
import com.otusfoss.core.designsystem.resources.google_sans_black
import com.otusfoss.core.designsystem.resources.google_sans_bold
import com.otusfoss.core.designsystem.resources.google_sans_regular
import com.otusfoss.core.designsystem.resources.google_sans_rounded_black
import com.otusfoss.core.designsystem.resources.google_sans_rounded_bold
import com.otusfoss.core.designsystem.resources.google_sans_rounded_regular
import com.otusfoss.core.designsystem.resources.google_sans_rounded_semibold
import com.otusfoss.core.designsystem.resources.google_sans_semibold
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

/** The font the user picked for every text. [System] is the platform's own: Roboto on Android, San Francisco on iOS. */
public enum class AppFont {
    System,
    GoogleSans,
    GoogleSansRounded,
    Figtree,
    FigtreeRounded,
}

/** The font [OtusTheme][com.otusfoss.core.designsystem.theme.OtusTheme] sets; [panelTextStyle] draws in it. */
internal val LocalAppFont: ProvidableCompositionLocal<AppFont> = staticCompositionLocalOf { AppFont.System }

/** [font] in every weight the suite uses. */
@Composable
internal fun fontFamily(font: AppFont): FontFamily =
    when (font) {
        AppFont.System -> {
            FontFamily.Default
        }

        AppFont.GoogleSans -> {
            bundled(
                Res.font.google_sans_regular,
                Res.font.google_sans_semibold,
                Res.font.google_sans_bold,
                Res.font.google_sans_black,
            )
        }

        AppFont.GoogleSansRounded -> {
            bundled(
                Res.font.google_sans_rounded_regular,
                Res.font.google_sans_rounded_semibold,
                Res.font.google_sans_rounded_bold,
                Res.font.google_sans_rounded_black,
            )
        }

        AppFont.Figtree -> {
            bundled(Res.font.figtree, Res.font.figtree_semibold, Res.font.figtree_bold, Res.font.figtree_black)
        }

        AppFont.FigtreeRounded -> {
            bundled(
                Res.font.figtree_round,
                Res.font.figtree_round_semibold,
                Res.font.figtree_round_bold,
                Res.font.figtree_round_black,
            )
        }
    }

/**
 * A bundled font, one static file per weight. A single variable file cannot serve several on
 * Android, which caches asset fonts by path and would draw every weight from whichever variation
 * loaded first. Bold and Black are what Normal and SemiBold become when the system's bold text
 * setting adds 300 to every weight; without them both would fall back to SemiBold and unread text
 * would look like the rest.
 */
@Composable
private fun bundled(
    regular: FontResource,
    semiBold: FontResource,
    bold: FontResource,
    black: FontResource,
): FontFamily {
    val fonts =
        listOf(
            Font(regular, FontWeight.Normal),
            Font(semiBold, FontWeight.SemiBold),
            Font(bold, FontWeight.Bold),
            Font(black, FontWeight.Black),
        )
    return remember(fonts) { FontFamily(fonts) }
}
