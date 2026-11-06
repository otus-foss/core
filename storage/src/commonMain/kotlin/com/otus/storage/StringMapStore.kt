package com.otus.storage

/** A map of strings kept on the device, read whole and replaced whole on each write. */
public interface StringMapStore {
    public fun read(): Map<String, String>

    public fun write(values: Map<String, String>)
}
