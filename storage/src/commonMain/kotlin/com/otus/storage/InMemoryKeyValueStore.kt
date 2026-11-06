package com.otus.storage

/** A [KeyValueStore] that keeps [values] in memory only, for previews and tests. */
public class InMemoryKeyValueStore(
    values: Map<String, Any> = emptyMap(),
) : KeyValueStore {
    private var saved: Map<String, Any> = values

    override fun string(key: String): String? = saved[key] as? String

    override fun flag(key: String): Boolean? = saved[key] as? Boolean

    override fun int(key: String): Int? = saved[key] as? Int

    override fun edit(changes: KeyValueEditor.() -> Unit) {
        saved = saved + buildMap { Editor(this).changes() }
    }

    private class Editor(
        private val changes: MutableMap<String, Any>,
    ) : KeyValueEditor {
        override fun put(
            key: String,
            value: String,
        ) {
            changes[key] = value
        }

        override fun put(
            key: String,
            value: Boolean,
        ) {
            changes[key] = value
        }

        override fun put(
            key: String,
            value: Int,
        ) {
            changes[key] = value
        }
    }
}
