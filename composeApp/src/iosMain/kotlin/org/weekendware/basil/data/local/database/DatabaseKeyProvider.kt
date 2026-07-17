package org.weekendware.basil.data.local.database

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFTypeRefVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
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

@OptIn(ExperimentalForeignApi::class)
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

                val result = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, result.ptr)
                CFRelease(query)

                if (status != errSecSuccess) return@memScoped null
                val dataRef = result.value ?: return@memScoped null
                val cfData = dataRef.reinterpret<platform.CoreFoundation.__CFData>()
                val len = CFDataGetLength(cfData).toInt()
                val ptr = CFDataGetBytePtr(cfData) ?: run { CFRelease(dataRef); return@memScoped null }
                ptr.reinterpret<ByteVar>().readBytes(len).decodeToString().also { CFRelease(dataRef) }
            }
        } finally {
            CFRelease(serviceRef)
            CFRelease(accountRef)
        }
    }

    private fun generateAndStore(): String {
        val key = memScoped {
            val buf = allocArray<UByteVar>(32)
            SecRandomCopyBytes(kSecRandomDefault, 32UL, buf)
            ByteArray(32) { buf[it].toByte() }
        }.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
        storeInKeychain(key)
        return key
    }

    private fun storeInKeychain(key: String) {
        val keyBytes = key.encodeToByteArray()
        val cfData = keyBytes.asUByteArray().let { ub ->
            ub.usePinned { CFDataCreate(null, it.addressOf(0), keyBytes.size.toLong())!! }
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
