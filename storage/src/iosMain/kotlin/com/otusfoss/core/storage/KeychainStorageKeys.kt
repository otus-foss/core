package com.otusfoss.core.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

private const val KEYS_BYTES = 64
private const val ACCOUNT = "storage"

/**
 * The device's storage keys, kept as the Keychain item of [service]. A first use makes them. While
 * the Keychain cannot be read or written, the keys last for this process only and a saved item is
 * left alone, so what was sealed reads again once the Keychain answers.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun keychainStorageKeys(service: String): ByteArray {
    val saved = readKeys(service)
    if (saved is SavedKeys.Found) return saved.bytes
    val fresh = randomBytes(KEYS_BYTES)
    if (saved is SavedKeys.Missing) addKeys(service, fresh)
    return fresh
}

private sealed interface SavedKeys {
    class Found(
        val bytes: ByteArray,
    ) : SavedKeys

    data object Missing : SavedKeys

    data object Unreadable : SavedKeys
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun readKeys(service: String): SavedKeys =
    withItem(service, kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne) { query ->
        memScoped {
            val result = alloc<CFTypeRefVar>()
            when (SecItemCopyMatching(query, result.ptr)) {
                errSecSuccess -> (CFBridgingRelease(result.value) as? NSData)?.let(::keysOf) ?: SavedKeys.Unreadable
                errSecItemNotFound -> SavedKeys.Missing
                else -> SavedKeys.Unreadable
            }
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun keysOf(data: NSData): SavedKeys {
    if (data.length.toInt() != KEYS_BYTES) return SavedKeys.Unreadable
    val bytes = ByteArray(KEYS_BYTES)
    bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return SavedKeys.Found(bytes)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun addKeys(
    service: String,
    keys: ByteArray,
) {
    val data = keys.usePinned { NSData.create(bytes = it.addressOf(0), length = keys.size.toULong()) }
    val retained = CFBridgingRetain(data)
    try {
        withItem(
            service,
            kSecValueData to retained,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        ) { SecItemAdd(it, null) }
    } finally {
        CFRelease(retained)
    }
}

/** Runs [block] with the Keychain query of [service]'s item and [more] attributes. */
@OptIn(ExperimentalForeignApi::class)
private fun <T> withItem(
    service: String,
    vararg more: Pair<CFTypeRef?, CFTypeRef?>,
    block: (CFDictionaryRef?) -> T,
): T {
    val query =
        CFDictionaryCreateMutable(null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
    val retainedService = CFBridgingRetain(service)
    val retainedAccount = CFBridgingRetain(ACCOUNT)
    return try {
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, retainedService)
        CFDictionaryAddValue(query, kSecAttrAccount, retainedAccount)
        more.forEach { (key, value) -> CFDictionaryAddValue(query, key, value) }
        block(query)
    } finally {
        CFRelease(retainedAccount)
        CFRelease(retainedService)
        CFRelease(query)
    }
}
