package com.otusfoss.core.storage

/**
 * Seals text kept on the device, so that only this app on this device reads it again. A platform's
 * cipher keeps its key in the Android Keystore or the iOS Keychain.
 */
public interface TextCipher {
    /**
     * [text] as sealed text. [context] names where it is kept, such as a store or a row; text opens
     * only with the context it was sealed with, so sealed values cannot be swapped.
     */
    public fun seal(
        text: String,
        context: String = "",
    ): String

    /** The text sealed into [sealed]; null when this cipher did not seal it under [context], or it changed. */
    public fun open(
        sealed: String,
        context: String = "",
    ): String?
}

/** Whether [text] has the form every [TextCipher] gives sealed text. Plain saved text never starts this way. */
public fun isSealedText(text: String): Boolean = text.startsWith(SEALED_PREFIX)

/** Starts every sealed text, with the format's version. */
public const val SEALED_PREFIX: String = "otus-sealed-1:"

/**
 * A [TextCipher] for previews and tests. It hides nothing: it only marks the text and ties it to
 * its context, so tests can tell sealed text from plain and see a swapped value refused.
 */
public class MarkingTextCipher : TextCipher {
    override fun seal(
        text: String,
        context: String,
    ): String = "$SEALED_PREFIX${context.length}:$context$text"

    override fun open(
        sealed: String,
        context: String,
    ): String? = sealed.removePrefixOrNull("$SEALED_PREFIX${context.length}:$context")
}

private fun String.removePrefixOrNull(prefix: String): String? =
    if (startsWith(prefix)) substring(prefix.length) else null
