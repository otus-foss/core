package com.otus.designsystem.shared.input.elements

import androidx.compose.ui.text.input.KeyboardType

/** What a field holds. It picks the keyboard; [Password] also masks the value. */
public enum class InputKind(
    internal val keyboardType: KeyboardType,
) {
    Text(KeyboardType.Text),
    Url(KeyboardType.Uri),
    Email(KeyboardType.Email),
    Phone(KeyboardType.Phone),
    Password(KeyboardType.Password),
}
