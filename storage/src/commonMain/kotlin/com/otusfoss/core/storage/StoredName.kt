package com.otusfoss.core.storage

/** A file-name-safe name for [value]: its SHA-256 digest in lowercase hex. */
public expect fun storedName(value: String): String
