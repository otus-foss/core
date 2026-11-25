package com.otusfoss.core.storage

/**
 * Seals text kept on the device, so that only this app on this device reads it again. A platform's
 * cipher keeps its key in the Android Keystore or the iOS Keychain.
 */
public interface TextCipher {
    /** How long this cipher's key lasts, which decides what a [SealedMapStore] may replace. */
    public val keyLife: KeyLife

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

/** How long a [TextCipher]'s key lasts. */
public enum class KeyLife {
    /**
     * The device's first key, made by this process. Only now may a store saved before sealing
     * began be taken in, since no later start can tell such a store from one planted in the files.
     */
    First,

    /** A key the device keeps, which later processes use again. */
    Kept,

    /**
     * A key for this process only, while the Keystore or Keychain does not answer. A sealed value
     * that does not open now may open again later, so it must not be replaced.
     */
    ProcessOnly,
}

/** Whether [text] has the form every [TextCipher] gives sealed text. Plain saved text never starts this way. */
public fun isSealedText(text: String): Boolean = text.startsWith(SEALED_PREFIX)

/** Starts every sealed text, with the format's version. */
public const val SEALED_PREFIX: String = "otus-sealed-1:"

/**
 * A [TextCipher] for previews and tests. It hides nothing: it only marks the text and ties it to
 * its context, so tests can tell sealed text from plain and see a swapped value refused.
 */
public class MarkingTextCipher(
    override val keyLife: KeyLife = KeyLife.Kept,
) : TextCipher {
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
