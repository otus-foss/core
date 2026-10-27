package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.theme.SummaryTokens

/**
 * [text] with [person]'s small avatar before the first place their name appears, so a person
 * named in a sentence reads like the people line of a
 * [SummaryCard][com.otus.designsystem.shared.rows.components.SummaryCard]. [person]'s name is matched
 * as written in [text], such as an address. Without a match, or without [person], it is plain text.
 */
@Composable
public fun PersonText(
    text: String,
    person: NamedAvatar?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    val at = person?.name?.takeIf { it.isNotEmpty() }?.let { text.indexOf(it) } ?: -1
    if (person == null || at < 0) {
        Text(text, modifier, color = color, fontSize = fontSize, lineHeight = lineHeight)
        return
    }
    val annotated =
        buildAnnotatedString {
            append(text.take(at))
            appendInlineContent(AVATAR)
            append(text.drop(at))
        }
    val size = with(LocalDensity.current) { (SummaryTokens.avatarSize + SummaryTokens.avatarGap).toSp() }
    val avatar =
        InlineTextContent(Placeholder(size, size, PlaceholderVerticalAlign.TextCenter)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) { SmallAvatar(person.avatar) }
        }
    Text(
        annotated,
        // Screen readers read the words alone, without the avatar's placeholder character.
        modifier.clearAndSetSemantics { this.text = AnnotatedString(text) },
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        inlineContent = mapOf(AVATAR to avatar),
    )
}

/**
 * The avatar a [SummaryCard][com.otus.designsystem.shared.rows.components.SummaryCard] or [PersonText] shows before
 * a person's name.
 */
@Composable
internal fun SmallAvatar(avatar: ChipAvatar) {
    PersonAvatar(avatar, SummaryTokens.avatarSize)
}

private const val AVATAR = "avatar"
