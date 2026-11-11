package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A person in the same outlined pill as a [RecipientChip], where they are only shown, such as the
 * people a message went to. With [onClick] the chip is a button in a 48 dp touch target, such as
 * to show who they are; without it, screen readers read it once, as text.
 */
@Composable
public fun PersonChip(
    person: Recipient,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    PersonPill(person.name, person.avatar, PersonPillSize.Regular, modifier, person.description, onClick)
}
