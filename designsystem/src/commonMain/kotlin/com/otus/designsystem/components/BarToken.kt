package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors

/** What an action bar's context is about, drawn at the start of its shell. */
public sealed interface BarToken {
    /** Tells two tokens apart, so a change of person or count crossfades and an equal one does not. */
    public val key: String

    /** One person, such as the sender a reply goes to. */
    public class Person(
        public val avatar: ChipAvatar,
    ) : BarToken {
        override val key: String = "person:${avatar.initials}:${avatar.colorIndex}"
    }

    /** Several people: the [first] one's avatar, then the [second] one's, or the number of others without it. */
    public class People(
        public val first: ChipAvatar,
        public val count: Int,
        public val second: ChipAvatar? = null,
    ) : BarToken {
        override val key: String = "people:${first.initials}:${second?.initials}:$count"
    }

    /** A number of items, such as selected messages. */
    public class Count(
        public val count: Int,
    ) : BarToken {
        override val key: String = "count:$count"
    }

    /** A day, as a short [month] name above the [day] of the month. */
    public class Date(
        public val month: String,
        public val day: String,
    ) : BarToken {
        override val key: String = "date:$month:$day"
    }
}

/** Draws [token]; decorative, because the shell speaks for its context. */
@Composable
internal fun BarTokenView(token: BarToken) {
    when (token) {
        is BarToken.Person -> {
            TokenAvatar(token.avatar)
        }

        is BarToken.People -> {
            val overlap = if (token.second == null) ActionBarTokens.tokenOverlap else ActionBarTokens.pairOverlap
            Row(horizontalArrangement = Arrangement.spacedBy(-overlap)) {
                TokenAvatar(token.first)
                token.second?.let { RingedAvatar(it) } ?: NumberCircle("+${token.count - 1}", ring = true)
            }
        }

        is BarToken.Count -> {
            NumberCircle(token.count.toString(), ring = false)
        }

        is BarToken.Date -> {
            DateSquare(token)
        }
    }
}

@Composable
private fun TokenAvatar(avatar: ChipAvatar) {
    PersonAvatar(avatar, ActionBarTokens.tokenSize)
}

/** An avatar with a `bgDark` ring, which separates it from the avatar it overlaps. */
@Composable
private fun RingedAvatar(avatar: ChipAvatar) {
    PersonAvatar(
        avatar,
        ActionBarTokens.tokenSize,
        Modifier.border(ActionBarTokens.tokenRing, OtusColors.bgDark, CircleShape),
    )
}

/** A bgLight circle; [ring] separates it from an avatar it overlaps. */
@Composable
private fun NumberCircle(
    text: String,
    ring: Boolean,
) {
    Avatar(
        text,
        if (ring) Modifier.border(ActionBarTokens.tokenRing, OtusColors.bgDark, CircleShape) else Modifier,
        fill = OtusColors.bgLight,
        size = ActionBarTokens.tokenSize,
    )
}

@Composable
private fun DateSquare(date: BarToken.Date) {
    Column(
        Modifier
            .size(ActionBarTokens.tokenSize)
            .clip(RoundedCornerShape(ActionBarTokens.dateRadius))
            .background(OtusColors.bgLight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            date.month.uppercase(),
            color = OtusColors.text2,
            fontSize = ActionBarTokens.dateMonthSize,
            lineHeight = ActionBarTokens.dateMonthSize,
        )
        Text(
            date.day,
            fontSize = ActionBarTokens.dateDaySize,
            lineHeight = ActionBarTokens.dateDaySize,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
