package com.neptools.app.core.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import com.neptools.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest

/**
 * NepTools Security Guard
 *
 * Runtime Application Self-Protection (RASP) engine. This engine is advisory: it reports
 * environment integrity so the app can surface it to the user and make informed decisions.
 * It never terminates the process on its own, because rooted devices and emulators are
 * legitimate configurations for many NepTools users.
 *
 * Checks performed:
 * 1. Active debugger / tracer inspection (/proc/self/status TracerPid)
 * 2. Dynamic hooking framework detection (Frida, Xposed, Substrate)
 * 3. Root / SU binary detection
 * 4. Signing certificate SHA-256 fingerprint validation
 *
 * The native C++ layer contributes an additional ptrace-based tracer check through
 * [NativeSecurityBridge]; its result is folded into [SecurityAuditReport.nativeIntegrityOk].
 */
object NepToolsSecurityGuard {

    /**
     * SHA-256 fingerprints of the certificates this app is allowed to be signed with.
     *
     * IMPORTANT: if you move to Google Play App Signing, Google re-signs the APK with the
     * Play signing key. Add that certificate's SHA-256 here, or the signature check will
     * report a false positive on production installs.
     */
    private val RELEASE_SIGNATURE_HASHES = setOf(
        "59F95B14D6BE151E03F87B7DCE59CCD73D979D034FFBE7D4D9EF7C13727D66A9"
    )

    /**
     * The Android debug keystore is public and identical on every machine, so it is only
     * accepted for debug builds.
     */
    private val DEBUG_SIGNATURE_HASHES = setOf(
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
        val nativeLayerAvailable: Boolean,
        val nativeIntegrityOk: Boolean?,
        val violations: List<String>
    )

    data class SecurityStatus(
        val report: SecurityAuditReport? = null,
        val isAuditing: Boolean = false
    )

    private val _status = MutableStateFlow(SecurityStatus())
    val status: StateFlow<SecurityStatus> = _status.asStateFlow()

    /**
     * Runs the full audit off the main thread and publishes the result to [status].
     */
    suspend fun refresh(context: Context) {
        _status.value = _status.value.copy(isAuditing = true)
        val report = withContext(Dispatchers.IO) {
            performSecurityAudit(context.applicationContext)
        }
        _status.value = SecurityStatus(report = report, isAuditing = false)
    }

    /**
     * Executes a full multi-point security audit.
     */
    fun performSecurityAudit(context: Context, enforceStrictTermination: Boolean = false): SecurityAuditReport {
        val violations = mutableListOf<String>()

        val debuggerDetected = isDebuggerAttached()
        if (debuggerDetected) {
            violations.add("Active debugger or tracer process detected")
        }

        val hookingDetected = isHookingFrameworkDetected()
        if (hookingDetected) {
            violations.add("Dynamic hooking or instrumentation framework detected in memory")
        }

        val rootDetected = isDeviceRooted()
        if (rootDetected) {
            violations.add("Device root binaries or insecure environment detected")
        }

        val certSha256 = getSigningCertificateSha256(context)
        val signatureValid = isAppSignatureValid(context, certSha256)
        if (!signatureValid) {
            violations.add("APK signature mismatch: potential tampering or repackaging")
        }

        val nativeAvailable = NativeSecurityBridge.isNativeLoaded
        val nativeOk = NativeSecurityBridge.nativeIntegrityOk(context)
        if (nativeOk == false) {
            violations.add("Native tracer detected by the ptrace integrity check")
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
            nativeLayerAvailable = nativeAvailable,
            nativeIntegrityOk = nativeOk,
            violations = violations
        )
    }

    /**
     * Detects if a debugger is attached via Android Debug API or /proc/self/status TracerPid.
     */
    fun isDebuggerAttached(): Boolean {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }

        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val current = line ?: continue
                        if (current.startsWith("TracerPid:", ignoreCase = true)) {
                            val tracerPid = current.substringAfter(":").trim().toIntOrNull() ?: 0
                            if (tracerPid > 0) return true
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // procfs read is restricted on some hardened kernels; treat as inconclusive
        }

        return false
    }

    /**
     * Scans /proc/self/maps for injected libraries (Frida, Xposed, Substrate)
     * and probes default Frida communication ports.
     */
    fun isHookingFrameworkDetected(): Boolean {
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val lowerLine = line?.lowercase() ?: continue
                        for (keyword in SUSPICIOUS_MAP_KEYWORDS) {
                            if (lowerLine.contains(keyword)) return true
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore procfs read errors
        }

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

        for (port in FRIDA_PORTS) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 50)
                    return true
                }
            } catch (_: Exception) {
                // Expected when Frida is not running
            }
        }

        return false
    }

    private val FRIDA_PORTS = intArrayOf(27042, 27043)

    /**
     * Checks for the presence of root binaries and test-keys build tags.
     */
    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        for (path in SUSPICIOUS_ROOT_PATHS) {
            if (File(path).exists()) return true
        }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                if (reader.readLine() != null) return true
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
            md.digest(firstSignature.toByteArray()).joinToString("") { "%02X".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    fun allowedSignatureHashes(): Set<String> = if (BuildConfig.DEBUG) {
        RELEASE_SIGNATURE_HASHES + DEBUG_SIGNATURE_HASHES
    } else {
        RELEASE_SIGNATURE_HASHES
    }

    /**
     * Verifies the app's signing certificate against the allow-list for the current build type.
     */
    fun isAppSignatureValid(
        context: Context,
        calculatedHash: String = getSigningCertificateSha256(context)
    ): Boolean {
        if (calculatedHash.isBlank()) return false
        val normalized = normalize(calculatedHash)
        return allowedSignatureHashes().any { normalize(it) == normalized }
    }

    private fun normalize(hash: String): String = hash.replace(":", "").uppercase()

    /**
     * Terminates the process when a caller explicitly opts into strict enforcement.
     */
    fun terminateApplication() {
        Process.killProcess(Process.myPid())
        kotlin.system.exitProcess(0)
    }

    /** Returns true for build types that must not ship to users. */
    fun isDebuggableBuild(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
