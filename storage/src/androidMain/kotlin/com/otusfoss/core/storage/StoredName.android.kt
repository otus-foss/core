package com.otusfoss.core.storage

import java.security.MessageDigest

public actual fun storedName(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.encodeToByteArray()).toHexString()
