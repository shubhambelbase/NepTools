package com.neptools.app.core.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import kotlin.system.exitProcess

/**
 * NepTools Security Guard
 * Production Runtime Application Self-Protection (RASP) Engine
 * 
 * Implements:
 * 1. Active Debugger & TracerPid inspection
 * 2. Dynamic Hooking Framework (Frida, Xposed, Substrate) scanning
 * 3. Root & SU Binary Integrity checks
 * 4. Signing Certificate SHA-256 Fingerprint validation
 */
object NepToolsSecurityGuard {

    // Known release & debug signing certificate SHA-256 fingerprints (Upper-case Hex)
    private val ALLOWED_SIGNATURE_HASHES = setOf(
        // Production & Keystore SHA-256
        "0674DAEB62AA40EF2495D9AA52F3C9D3C7CB3A22ED533B69DA4923A0C1471635"
    )

    private val SUSPICIOUS_MAP_KEYWORDS = arrayOf(
        "frida",
        "gadget",
        "linjector",
        "xposed",
        "xposedbridge",
        "substrate",
        "subhook",
        "cydia"
    )

    private val SUSPICIOUS_ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/data/adb/magisk",
        "/sbin/.magisk"
    )

    data class SecurityAuditReport(
        val isSecure: Boolean,
        val isDebuggerDetected: Boolean,
        val isHookingDetected: Boolean,
        val isRootDetected: Boolean,
        val isSignatureValid: Boolean,
        val signatureSha256: String,
        val violations: List<String>
    )

    /**
     * Executes a full multi-point security audit.
     */
    fun performSecurityAudit(context: Context, enforceStrictTermination: Boolean = false): SecurityAuditReport {
        val violations = mutableListOf<String>()

        // 1. Debugger Check
        val debuggerDetected = isDebuggerAttached()
        if (debuggerDetected) {
            violations.add("Active debugger or tracer process detected")
        }

        // 2. Hooking Framework Check (Frida / Xposed)
        val hookingDetected = isHookingFrameworkDetected()
        if (hookingDetected) {
            violations.add("Dynamic hooking or instrumentation framework detected in memory")
        }

        // 3. Root Check
        val rootDetected = isDeviceRooted()
        if (rootDetected) {
            violations.add("Device root binaries or insecure environment detected")
        }

        // 4. Signing Certificate Check
        val certSha256 = getSigningCertificateSha256(context)
        val signatureValid = isAppSignatureValid(context, certSha256)
        if (!signatureValid) {
            violations.add("APK signature mismatch: potential tampering or repackaging")
        }

        val isSecure = violations.isEmpty()

        if (!isSecure && enforceStrictTermination) {
            terminateApplication()
        }

        return SecurityAuditReport(
            isSecure = isSecure,
            isDebuggerDetected = debuggerDetected,
            isHookingDetected = hookingDetected,
            isRootDetected = rootDetected,
            isSignatureValid = signatureValid,
            signatureSha256 = certSha256,
            violations = violations
        )
    }

    /**
     * Detects if a debugger is attached via Android Debug API or /proc/self/status TracerPid.
     */
    fun isDebuggerAttached(): Boolean {
        // Standard Android Debug API checks
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }

        // Native TracerPid check via Linux procfs
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.startsWith("TracerPid:", ignoreCase = true) == true) {
                            val tracerPid = line?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
                            if (tracerPid > 0) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore procfs read errors
        }

        return false
    }

    /**
     * Scans /proc/self/maps for injected libraries (Frida, Xposed, Substrate)
     * and probes default Frida communication ports.
     */
    fun isHookingFrameworkDetected(): Boolean {
        // 1. Check memory maps for injected libraries
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val lowerLine = line?.lowercase() ?: continue
                        for (keyword in SUSPICIOUS_MAP_KEYWORDS) {
                            if (lowerLine.contains(keyword)) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore procfs read errors
        }

        // 2. Check for known Hooking classes in the ClassLoader
        val hookClasses = arrayOf(
            "de.robv.android.xposed.XposedBridge",
            "com.saurik.substrate.MS\$MethodPointer",
            "com.topjohnwu.magisk.core.MagiskBridge"
        )
        for (className in hookClasses) {
            try {
                Class.forName(className)
                return true
            } catch (_: ClassNotFoundException) {
                // Normal behavior
            }
        }

        // 3. Probe local Frida agent TCP ports (27042, 27043)
        val fridaPorts = intArrayOf(27042, 27043)
        for (port in fridaPorts) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 50)
                    return true // Port is open, Frida server likely running
                }
            } catch (_: Exception) {
                // Expected when Frida is not running
            }
        }

        return false
    }

    /**
     * Checks for the presence of root binaries and test-keys build tags.
     */
    fun isDeviceRooted(): Boolean {
        // 1. Build Tags check
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // 2. Direct binary path checks
        for (path in SUSPICIOUS_ROOT_PATHS) {
            if (File(path).exists()) {
                return true
            }
        }

        // 3. Execution check for 'su'
        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                if (reader.readLine() != null) {
                    return true
                }
            }
        } catch (_: Exception) {
            // Normal behavior on non-rooted systems
        }

        return false
    }

    /**
     * Extracts the SHA-256 fingerprint of the app's signing certificate.
     */
    fun getSigningCertificateSha256(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val firstSignature = signatures?.firstOrNull() ?: return ""
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(firstSignature.toByteArray())
            digest.joinToString("") { "%02X".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Verifies the app's signing certificate against allowed known hashes.
     */
    fun isAppSignatureValid(context: Context, calculatedHash: String = getSigningCertificateSha256(context)): Boolean {
        if (calculatedHash.isBlank()) return false
        val normalized = calculatedHash.replace(":", "").uppercase()
        // If placeholder is present in development, allow debug key match
        return ALLOWED_SIGNATURE_HASHES.any { allowed ->
            val cleanAllowed = allowed.replace(":", "").uppercase()
            cleanAllowed == normalized
        }
    }

    /**
     * Safely terminates the process in case of critical security tampering.
     */
    fun terminateApplication() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
