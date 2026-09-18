# NepTools Release Hardening Architecture

This document describes the defenses that are actually compiled into a release build. Every
claim below maps to a file and a code path. If you change a defense, update this document in the
same change; a document that overstates protection is worse than no document, because the next
engineer trusts it.

## Overview

| Layer | Mechanism | Implementation | Runs at |
| --- | --- | --- | --- |
| 1 | R8 shrinking, renaming, log stripping | `app/proguard-rules.pro` | Build time |
| 2 | Runtime environment audit (RASP) | `core/security/NepToolsSecurityGuard.kt` | App start, on demand from Settings |
| 3 | Native ptrace tracer check + symbol hiding | `app/src/main/cpp/native-lib.cpp`, `core/security/NativeSecurityBridge.kt` | App start (only when the native library loads) |
| 4 | Release signing + install-time APK verification | `keystore.properties`, `core/updater/GitHubUpdateManager.kt` | Build time, update install |

Layer 2 is advisory. It reports, it does not terminate the process. Rooted devices and emulators
are legitimate configurations for many NepTools users, so the app must not destroy their data or
refuse to run. The audit result is shown in Settings under "Security & Integrity".

## Layer 1: R8 / ProGuard

Configured in `app/proguard-rules.pro`, applied by `isMinifyEnabled = true` and
`isShrinkResources = true` in `buildTypes.release`.

What is actually done:
* `-allowaccessmodification` allows cross-class access changes that help inlining.
* `-assumenosideeffects` removes every `android.util.Log` call from release builds.
* `-renamesourcefileattribute SourceFile` hides original file names.
* `-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions` keeps what
  Compose, reflection, and Kotlin metadata need.

Important caveat: the keep rules intentionally pin large parts of the app
(`core.data.**`, `core.calendar.**`, `core.habit.**`, `core.updater.**`, `core.vault.**`,
`astrology.**`, `core.util.**`). Those packages are **not** obfuscated, because they are loaded
through reflective or field-access patterns that R8 cannot prove safe to rename. Narrowing the
keep rules is a real improvement opportunity, but it must be done with instrumentation tests,
not by deleting rules and hoping.

## Layer 2: Runtime environment audit (RASP)

`core/security/NepToolsSecurityGuard.kt`, invoked from `PatroApp.onCreate()` on a background
dispatcher and republished to `NepToolsSecurityGuard.status` as a `StateFlow`.

Checks:
1. **Debugger / tracer**: `Debug.isDebuggerConnected()`, `Debug.waitingForDebugger()`, and
   `TracerPid` parsed from `/proc/self/status`.
2. **Hooking frameworks**: `/proc/self/maps` scanned for `frida`, `gadget`, `linjector`,
   `xposed`, `substrate`; TCP probes against 127.0.0.1:27042 and 27043; reflection checks for
   known Xposed/Substrate/Magisk bridge classes.
3. **Root indicators**: SU binary paths, `test-keys` build tags, `which su`.
4. **Signing certificate**: SHA-256 of the APK signing certificate compared against an allow-list.

Signature allow-list rules:
* `RELEASE_SIGNATURE_HASHES` holds the release certificate fingerprint.
* `DEBUG_SIGNATURE_HASHES` holds the public Android debug key fingerprint and is only accepted
  when `BuildConfig.DEBUG` is true.
* If Google Play App Signing is ever used, the Play signing certificate must be added to the
  release set, or production installs will report a false signature failure.
* A signature hash mismatch (for example a missing certificate) is reported, not acted on.

`performSecurityAudit(context, enforceStrictTermination = true)` exists for callers that
genuinely need to kill the process. Nothing calls it that way today, and no new caller should
without a product decision, because it destroys user data on rooted devices.

## Layer 3: Native security library

`app/src/main/cpp/` is built through `externalNativeBuild` in `app/build.gradle.kts`. Before this
wiring existed the CMake project was never compiled, so `System.loadLibrary("neptools-security")`
always threw `UnsatisfiedLinkError` and the layer was inert.

What it does:
* `verifyEnvironmentIntegrity()` performs a `ptrace(PTRACE_TRACEME)` check and inspects
  `/proc/self/wchan` for an active tracer.
* Methods are bound with `RegisterNatives` inside `JNI_OnLoad`, so no `Java_*` symbols are
  exported for a decompiler to read.
* `-fvisibility=hidden` and `-Wl,--strip-all` keep the symbol table minimal.
* ABI filters limit the build to `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

What it deliberately does **not** do: hold vault key material. An earlier revision embedded an
XOR-obfuscated "vault seed". Anything compiled into a shipped APK is recoverable, so a compiled-in
secret provides no cryptographic strength, and `VaultCrypto` never used it. The seed was removed.
Vault keys are derived from the user's master password and a 32-byte random salt with
PBKDF2-HMAC-SHA256 at 210,000 iterations, and the data key is sealed with AES-256-GCM.

`NativeSecurityBridge.nativeIntegrityOk()` returns `null` when the library is unavailable, and
that null is reported as "unknown" rather than either "secure" or "compromised".

## Layer 4: Signing and update verification

Release signing is configured in `app/build.gradle.kts` from `keystore.properties`, which is
gitignored. See `RELEASE_SIGNING.md` for backup and migration steps. Do not distribute a build
that fell back to the debug key; the build prints a warning when it does.

`core/updater/GitHubUpdateManager.kt` verifies a downloaded APK before it is handed to the
package installer:
1. Minimum size sanity check.
2. SHA-256 of the file compared with the checksum published in the release notes. A checksum is
   **required** for an automatic install; without one the updater refuses and asks the user to
   install from the GitHub release page. The package-name check alone is not a security control,
   because an attacker-authored APK simply declares the same package name.
3. `PackageManager.getPackageArchiveInfo()` archive parse and package identity check.

## What is intentionally not claimed

* **No TLS certificate pinning.** Traffic uses `HttpURLConnection` over HTTPS with the system CA
  store, plus the update checksum above. Adding pins is a deliberate future decision because a
  bad pin bricks updates.
* **No tamper-resistant secret storage.** Anything shipped in the APK is extractable.
* **No remote kill switch or anti-tamper termination.**
* **The remote emergency-contact feed is not signed.** It is instead merged additively: remote
  entries can add contacts but can never overwrite or remove a compiled-in hotline
  (`EmergencyRepo.mergeRemoteContacts`). Payloads must pass number-format validation, a
  minimum-size floor, and version monotonicity before being cached.

## Release checklist

1. `keystore.properties` present and pointing at the release keystore, and the build log free of
   the debug-key fallback warning.
2. `RELEASE_SIGNATURE_HASHES` matches the certificate actually used for this build.
3. `./gradlew testDebugUnitTest` and `./gradlew lintDebug` pass; CI enforces both.
4. `versionCode` incremented in `app/build.gradle.kts`.
5. SHA-256 of the APK published in the GitHub release notes, so the in-app updater can verify it.
