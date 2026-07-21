package org.weekendware.basil.data.local.database

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecRandomDefault
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual class DatabaseKeyProvider {

    actual fun getOrCreateKey(): String = loadFromKeychain() ?: generateAndStore()

    private fun loadFromKeychain(): String? {
        val serviceRef = CFStringCreateWithCString(null, SERVICE, kCFStringEncodingUTF8)!!
        val accountRef = CFStringCreateWithCString(null, ACCOUNT, kCFStringEncodingUTF8)!!
        return try {
            memScoped {
                val query = CFDictionaryCreateMutable(null, 4, null, null)!!
                CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionarySetValue(query, kSecAttrService, serviceRef)
                CFDictionarySetValue(query, kSecAttrAccount, accountRef)
                CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)

                val result = alloc<ObjCObjectVar<*>>()
                val status = SecItemCopyMatching(query, result.ptr.reinterpret())
                CFRelease(query)

                if (status != errSecSuccess) return@memScoped null
                // NSData is ARC-managed — do not CFRelease.
                // NSData.getBytes is not exposed in K2 bindings. Use base64EncodedStringWithOptions
                // (returns String via NSString bridge) then decode in Kotlin to get original bytes.
                val data = result.value as? NSData ?: return@memScoped null
                val b64 = data.base64EncodedStringWithOptions(0UL) ?: return@memScoped null
                Base64.decode(b64).decodeToString()
            }
        } finally {
            CFRelease(serviceRef)
            CFRelease(accountRef)
        }
    }

    private fun generateAndStore(): String {
        val keyBytes = ByteArray(32)
        // SecRandomCopyBytes uses Apple's CSPRNG (backed by /dev/random on iOS).
        // Random.Default is XorWowRandom — not cryptographically secure.
        keyBytes.usePinned { pinned ->
            val result = SecRandomCopyBytes(kSecRandomDefault, 32UL, pinned.addressOf(0))
            check(result == 0) { "SecRandomCopyBytes failed: $result" }
        }
        val key = keyBytes.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
        storeInKeychain(key)
        return key
    }

    private fun storeInKeychain(key: String) {
        val keyBytes = key.encodeToByteArray()
        // usePinned holds the ByteArray in place; CFDataCreate copies the bytes internally,
        // so cfData is valid and independently retained after the usePinned scope ends.
        val cfData = keyBytes.usePinned { pinned ->
            CFDataCreate(null, pinned.addressOf(0).reinterpret(), keyBytes.size.toLong())!!
        }
        val serviceRef = CFStringCreateWithCString(null, SERVICE, kCFStringEncodingUTF8)!!
        val accountRef = CFStringCreateWithCString(null, ACCOUNT, kCFStringEncodingUTF8)!!
        try {
            val attrs = CFDictionaryCreateMutable(null, 5, null, null)!!
            CFDictionarySetValue(attrs, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(attrs, kSecAttrService, serviceRef)
            CFDictionarySetValue(attrs, kSecAttrAccount, accountRef)
            CFDictionarySetValue(attrs, kSecValueData, cfData)
            CFDictionarySetValue(attrs, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            SecItemAdd(attrs, null)
            CFRelease(attrs)
        } finally {
            CFRelease(cfData)
            CFRelease(serviceRef)
            CFRelease(accountRef)
        }
    }

    private companion object {
        const val SERVICE = "org.weekendware.basil"
        const val ACCOUNT = "db_encryption_key"
    }
}
