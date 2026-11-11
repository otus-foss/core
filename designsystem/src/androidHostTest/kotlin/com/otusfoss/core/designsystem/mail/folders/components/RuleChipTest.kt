package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_short_text
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.rows.components.RowDensity
import com.otusfoss.core.designsystem.theme.tokens.selection.RuleChipTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RuleChipTest {
    @get:Rule
    val compose = createComposeRule()

    private val startsWith =
        RuleChipLine(
            "Subject",
            RuleChipValue(listOf(RuleSegment("starts with ", RuleSegmentStyle.Dim), RuleSegment("\"[Acme]\""))),
            icon = Res.drawable.symbol_short_text,
        )
    private val domain =
        RuleChipLine("From", RuleChipValue(listOf(RuleSegment("@acme.example"))), avatar = ChipAvatar("A", 1))

    @Test
    fun aChipIsOneButtonThatReadsItsLabelAndValue() {
        var clicks = 0
        compose.setContent { RuleChip(startsWith, onClick = { clicks++ }) }
        compose
            .onNodeWithText("Subject", substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsEqualTo(RuleChipTokens.height)
            .performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun aCompactChipIsTwentyFourDpTall() {
        compose.setContent { RuleChip(domain, density = RowDensity.Compact) }
        compose.onNodeWithText("From", substring = true).assertHeightIsEqualTo(RuleChipTokens.compactHeight)
    }

    @Test
    fun aGroupIsOneButtonOverItsConditions() {
        var clicks = 0
        compose.setContent { Column { RuleChipGroup("ALL OF", listOf(domain, startsWith), onClick = { clicks++ }) } }
        compose.onNodeWithText("ALL OF", substring = true).performClick()
        assertEquals(1, clicks)
    }
}
