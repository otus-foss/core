package com.otus.designsystem.shared.people.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** A person's avatar and name in an outlined pill; tapping it edits or removes them. */
@Composable
public fun RecipientChip(
    recipient: Recipient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PersonPillFrame(
        recipient.name,
        recipient.avatar,
        PersonPillSize.Regular,
        modifier,
        recipient.description,
        Modifier.clickable(onClickLabel = "Edit", role = Role.Button, onClick = onClick),
    )
}
