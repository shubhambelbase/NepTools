package com.neptools.app.core.security

import android.content.Context

/**
 * Kotlin JNI bridge for the NepTools native security engine.
 *
 * The native library only contributes a ptrace-based tracer check. It intentionally does NOT
 * hold vault key material: a secret compiled into the APK is extractable from the shipped
 * binary, so it must not be used for key derivation. Vault keys are derived from the user's
 * master password and a per-vault random salt (see VaultCrypto).
 *
 * Native methods are registered dynamically in JNI_OnLoad, so no Java_* symbols are exported.
 */
object NativeSecurityBridge {

    val isNativeLoaded: Boolean = try {
        System.loadLibrary("neptools-security")
        true
    } catch (_: UnsatisfiedLinkError) {
        false
    } catch (_: Exception) {
        false
    }

    /**
     * Native ptrace / tracer verification.
     */
    private external fun verifyEnvironmentIntegrity(context: Context): Boolean

    /**
     * @return true when the native layer reports a clean environment, false when it detects a
     * tracer, or null when the native layer is unavailable or errored. Null is reported as
     * "unknown" rather than being treated as either secure or tampered with.
     */
    fun nativeIntegrityOk(context: Context): Boolean? {
        if (!isNativeLoaded) return null
        return try {
            verifyEnvironmentIntegrity(context)
        } catch (_: Throwable) {
            null
        }
    }
}
