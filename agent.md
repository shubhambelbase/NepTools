# 🛡️ NepTools — Release Build Anti-Decompile & Multi-Layer Defense Architecture

This guide details the complete 4-layer defense-in-depth security architecture designed to protect **NepTools** against reverse engineering, bytecode decompilation (JADX, Bytecode Viewer), dynamic memory manipulation (Frida, Xposed, GameGuardian), native disassembly (Ghidra, IDA Pro), and APK repackaging.

---

## 🏗️ Defense-in-Depth Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│                        NepTools Security Perimeter                     │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 1: Aggressive R8/ProGuard Obfuscation & Bytecode Repackaging    │
│  ├── Package flattening (-repackageclasses '')                         │
│  ├── Zero Log footprint (-assumenosideeffects android.util.Log)        │
│  └── SourceFile & line number obfuscation                              │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 2: Runtime Application Self-Protection (RASP Engine)             │
│  ├── TracerPid & Linux procfs active debugger detection                │
│  ├── /proc/self/maps memory scanning (Frida, Xposed, Substrate)        │
│  ├── Localhost Frida TCP probe (Ports 27042 / 27043)                   │
│  ├── Multi-path Root & SU binary detection                             │
│  └── Release Signing Certificate SHA-256 Fingerprint Pinning           │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 3: Native C++ Isolation & Dynamic JNI Symbol Hiding              │
│  ├── Dynamic JNI registration via JNI_OnLoad (No Java_ export symbols) │
│  ├── Compiler symbol stripping (-fvisibility=hidden -Wl,--strip-all)   │
│  ├── Native ptrace(PTRACE_TRACEME) anti-debugging trap                 │
│  └── Dynamic XOR-masked AES-256 Vault seed generation                  │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 4: Cryptographic In-App Update Verification                      │
│  ├── Streaming SHA-256 hash calculation prior to installation          │
│  ├── PackageArchiveInfo validation to prevent APK identity hijacking   │
│  └── Safe scoped installation via Android FileProvider                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🔒 Layer 1: Aggressive R8 / ProGuard Configuration

**Target File**: `app/proguard-rules.pro`

### 1. Class Repackaging & Name Mangling
When reverse engineers decompile an Android APK using **JADX**, class directory structures normally expose package names (e.g. `com.neptools.app.core.vault`). 

R8 aggressive repackaging moves all non-public classes into the default root package `""` or single-letter namespaces, making class structure flat and unreadable:

```proguard
# Flatten all classes into the root package
-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5
-mergeinterfacesaggressively

# Obfuscate source file names
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
```

### 2. Stripping All Debug Logs
Logging statements (`Log.d`, `Log.v`, `Log.i`, `Log.e`) leak application flow and sensitive API structures. We configure R8 to eliminate them completely from release bytecode:

```proguard
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(...);
}
```

### 3. Preserving Essential Framework Targets
To ensure Jetpack Compose, dynamic JNI, BiometricPrompt, and Kotlin Coroutines function properly under aggressive optimization:

```proguard
# Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Custom Vector Drawables (PatroIcons)
-keep class com.neptools.app.ui.icons.** { *; }

# Dynamic JNI Bridge
-keep class com.neptools.app.core.security.** { *; }
-keepclasseswithmembers class com.neptools.app.core.security.NativeSecurityBridge {
    native <methods>;
}

# Biometric & Keystore Crypto
-keep class androidx.biometric.** { *; }
-keep class javax.crypto.** { *; }
```

---

## 🛡️ Layer 2: Runtime Application Self-Protection (RASP)

**Target File**: `app/src/main/java/com/neptools/app/core/security/NepToolsSecurityGuard.kt`

The `NepToolsSecurityGuard` singleton runs comprehensive environment and memory integrity checks during app startup and critical operations:

### 1. Active Debugger & TracerPid Detection
Decompilers and reverse engineers attach GDB, LLDB, or JDWP debuggers. We detect them through dual inspection:
* Standard Android API: `Debug.isDebuggerConnected()` & `Debug.waitingForDebugger()`.
* Low-Level Linux `procfs`: Reading `/proc/self/status` line-by-line to parse `TracerPid`. If `TracerPid > 0`, an external process is actively tracing execution.

### 2. Dynamic Memory Hooking Detection (Frida / Xposed)
Frida injects `frida-agent.so` or `frida-gadget.so` into the running process. We detect this by:
* Reading `/proc/self/maps` in real-time to inspect all loaded `.so` and `.jar` memory mappings for suspicious keywords (`frida`, `gadget`, `linjector`, `xposed`, `substrate`).
* Probing default local Frida TCP listening ports (`27042`, `27043`).
* Reflection checks for hooking class loaders (`de.robv.android.xposed.XposedBridge`).

### 3. Multi-Vector Root Detection
Scans for standard SU binary locations across the filesystem (`/system/bin/su`, `/sbin/su`, `/data/local/xbin/su`, `/data/adb/magisk`), test-keys build tags, and attempts executing `which su`.

### 4. APK Signature SHA-256 Fingerprint Pinning
Prevents repackaging attacks where an attacker modifies the APK, re-signs it with a custom certificate, and redistributes it:
* Extracts the X.509 signing certificate from `PackageManager`.
* Computes its SHA-256 cryptographic digest.
* Verifies against hardcoded allowed release hashes.

```kotlin
// Example Startup Invocation in MainActivity.kt or Application.onCreate():
val auditReport = NepToolsSecurityGuard.performSecurityAudit(context, enforceStrictTermination = false)
if (!auditReport.isSecure) {
    // Log internally or terminate process
    NepToolsSecurityGuard.terminateApplication()
}
```

---

## ⚙️ Layer 3: Native C++ Isolation & Dynamic JNI Registration

**Target Files**: 
* `app/src/main/cpp/native-lib.cpp`
* `app/src/main/cpp/CMakeLists.txt`
* `app/src/main/java/com/neptools/app/core/security/NativeSecurityBridge.kt`

### 1. Dynamic JNI Registration (Hiding Symbols from Ghidra/IDA Pro)
Standard JNI functions use naming schemes like:
`Java_com_neptools_app_core_security_NativeSecurityBridge_getVaultSeed`

These names appear clearly in decompilers under the `.dynsym` table.

We use **Dynamic JNI Registration** inside `JNI_OnLoad` via `env->RegisterNatives()`. The function pointers are registered at runtime and do **NOT** export any `Java_` symbol names.

### 2. Native ptrace Anti-Debugging
Linux allows only one tracer process per PID. In `native-lib.cpp`, calling:
```cpp
ptrace(PTRACE_TRACEME, 0, 1, 0);
```
If an attacker attached a debugger prior to this call, `ptrace` returns `-1`, alerting the app to immediately abort.

### 3. Obfuscated Vault Pepper Generation
Critical cryptographic salts and vault seeds are stored as XOR-encoded byte arrays with multi-stage rotating keys:
```cpp
decryptedBytes[i] = static_cast<jbyte>(OBFUSCATED_SEED[i] ^ NEUTRAL_KEY ^ (ROTATING_KEY + (i % 7)));
```

### 4. Compiler Flags in `CMakeLists.txt`
```cmake
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fvisibility=hidden -fvisibility-inlines-hidden -fstack-protector-strong -D_FORTIFY_SOURCE=2 -Wl,-z,relro,-z,now")
set_target_properties(neptools-security PROPERTIES LINK_FLAGS "-Wl,--strip-all -Wl,--exclude-libs,ALL")
```

---

## 🚀 Layer 4: Cryptographic In-App Update Verification

**Target File**: `app/src/main/java/com/neptools/app/core/updater/GitHubUpdateManager.kt`

To prevent Man-in-the-Middle (MITM) attacks and malicious APK substitution:

### 1. Streaming SHA-256 Digest Calculation
When downloading a release APK from GitHub, the app streams the byte stream into a `MessageDigest.getInstance("SHA-256")` pipeline using a 64KB memory buffer.

### 2. Pre-Install Verification Pipeline
Before invoking Android's `FileProvider` package installer:
1. **File Size Check**: Validates the APK meets the expected size threshold.
2. **SHA-256 Checksum Check**: Verifies the calculated hash against the published release hash.
3. **Archive Structure & Package Name Check**: Uses Android's `PackageManager.getPackageArchiveInfo()` to verify that the downloaded APK package name strictly equals `com.neptools.app`.

---

## 📋 Integration Checklist for Release Builds

1. **Verify ProGuard in `app/build.gradle.kts`**:
   Ensure `isMinifyEnabled = true` and `isShrinkResources = true` in `buildTypes.release`.

2. **Add Native Support (Optional C++ Build)**:
   If building the C++ native module, ensure `externalNativeBuild` is configured in `app/build.gradle.kts`:
   ```kotlin
   android {
       externalNativeBuild {
           cmake {
               path = file("src/main/cpp/CMakeLists.txt")
               version = "3.22.1"
           }
       }
   }
   ```

3. **Production Signing Keystore**:
   When switching from the debug key to your private production keystore:
   * Run: `keytool -list -v -keystore your_release_key.jks -alias your_alias`
   * Copy the **SHA-256** fingerprint.
   * Add the fingerprint to `NepToolsSecurityGuard.ALLOWED_SIGNATURE_HASHES`.

4. **Verify Obfuscation**:
   Build the release APK (`./gradlew :app:assembleRelease`) and open `app-release.apk` in Android Studio's **APK Analyzer** or **JADX-GUI** to inspect:
   * Class names are obfuscated (`a.a.b.c`).
   * No `android.util.Log` strings exist.
   * Native `.so` libraries contain no exported `Java_` symbols.
