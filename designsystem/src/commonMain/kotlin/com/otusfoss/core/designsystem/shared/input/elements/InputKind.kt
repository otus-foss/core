package com.otusfoss.core.designsystem.shared.input.elements

import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.KeyboardType

/**
 * What a field holds. It picks the keyboard; [Password] also masks the value. [Username] and
 * [Password] tell password managers and other autofill services which login field they are.
 */
public enum class InputKind(
    internal val keyboardType: KeyboardType,
    internal val contentType: ContentType? = null,
) {
    Text(KeyboardType.Text),
    Url(KeyboardType.Uri),
    Email(KeyboardType.Email),
    Phone(KeyboardType.Phone),
    Password(KeyboardType.Password, ContentType.Password),

    /** The name an account signs in with, often an email address, on the email keyboard. */
    Username(KeyboardType.Email, ContentType.Username),
}
