package com.neptools.app.core.security

import android.content.Context

/**
 * Kotlin JNI Bridge for NepTools Native Security Engine.
 * 
 * Dynamic method bindings registered in native-lib.cpp via RegisterNatives.
 */
object NativeSecurityBridge {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("neptools-security")
            isNativeLoaded = true
        } catch (_: UnsatisfiedLinkError) {
            isNativeLoaded = false
        } catch (_: Exception) {
            isNativeLoaded = false
        }
    }

    /**
     * Retrieves the de-obfuscated pepper/seed for AES-256 Vault key derivation.
     */
    external fun getVaultSeed(): ByteArray

    /**
     * Executes native-level anti-debugging and environment validation.
     */
    external fun verifyEnvironmentIntegrity(context: Context): Boolean

    /**
     * Safe wrapper for vault seed with fallback if native layer is unavailable.
     */
    fun getSecureVaultSeed(): ByteArray {
        return if (isNativeLoaded) {
            try {
                getVaultSeed()
            } catch (_: Exception) {
                fallbackSeed()
            }
        } else {
            fallbackSeed()
        }
    }

    /**
     * Safe wrapper for environment integrity check.
     */
    fun isNativeEnvironmentSecure(context: Context): Boolean {
        return if (isNativeLoaded) {
            try {
                verifyEnvironmentIntegrity(context)
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }

    private fun fallbackSeed(): ByteArray {
        // Obfuscated Kotlin fallback
        val raw = "NepTools_Security_Seed_FallBack_2026".toByteArray(Charsets.UTF_8)
        return raw.map { (it.toInt() xor 0x5A).toByte() }.toByteArray()
    }
}
