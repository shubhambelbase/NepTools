# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 36, compileSdk: 36)
- Current Version: v2.9.0 (versionCode: 49)
- Last Updated: September 29, 2026
- Languages: Kotlin (JVM 17) + C++20 for native security
- UI Toolkit: 100% Jetpack Compose (Material 3) with Compose BOM
- Creator: Shubham Belbase
- Source Code Repository (Private): https://github.com/shubhambelbase/NepTools-private
- Distribution & Releases (Public): https://github.com/shubhambelbase/NepTools

---

## 2. Core Constraints & Conventions

1. Strict Emoji Ban: Do not use emojis anywhere (UI text, buttons, presets, logs, source code, comments, documentation).
2. Navigation: Exactly 3 bottom tabs: Home ("गृह"), Calendar ("पात्रो"), Tools ("टूल्स"). Top-right 3-line menu gives access to Updater, Settings, and About.
3. Card Design: Single clean title per card; no redundant sub-labels.
4. Offline-First: 100% offline for calendar, astrology, calculators, habit tracker, and vault. Zero telemetry, tracking, or ads.
5. Mandatory Version Bump: Every update / release must bump versionCode (+1) and versionName in app/build.gradle.kts, sync AGENT_CONTEXT.md, and verify SHA-256 before publishing release APKs.
6. Release Signing Is Non-Negotiable: `assembleRelease` throws if `keystore.properties` or `release.keystore` is missing. Never reintroduce a debug-key fallback; the Android debug keystore is public, so a release signed with it can be replaced by anyone holding that key.
7. Accessibility Floor: Body and secondary text must clear WCAG AA 4.5:1 against its backdrop, and interactive targets must be at least 48dp. Verify contrast before adding a colour to `Color.kt`.
8. Never Block the Main Thread: Ephemeris solves, JSON dataset parsing, bitmap compression and file IO belong in `Dispatchers.IO` or `Dispatchers.Default`. `remember` is not an offload mechanism.

---

## 3. Technology Stack & Key Engines

- UI & Theme: Jetpack Compose + Material 3. Custom NepToolsTheme (Newari Ink / Rice Paper palette) with a `ThemeMode` enum (System / Light / Dark) resolved via `isSystemInDarkTheme()`; the window background is painted from the resolved mode in MainActivity to avoid a cold-start colour step. ToolTopBar in PatroComponents.kt establishes unified typography (titleMedium 16.5sp, bold, -0.2sp tracking + 11sp onSurfaceVariant subtitle), 36dp surface action pill back navigation, zero emojis.
- Navigation: Compose Navigation (PatroNavHost.kt) with animated transitions. External entry points pass a route string via the `navigate_to_route` Intent extra, so `Routes.DEEPLINK_TARGETS` is the allowlist; unknown routes are dropped rather than navigated to.
- Calendar & Astronomy:
  - BsCalendarEngine.kt: Bikram Sambat date math (BS 1970 to BS 2100+).
  - SolarCalc.kt: NOAA solar engine for live sunrise, sunset, and Rahu Kaal from GPS or selected district coordinates.
  - PanchangCalc.kt & DynamicFestivalEngine.kt: Tithi, Nakshatra, Yoga, and festive observances. A single `compute()` costs roughly 21 ephemeris evaluations, so never call it in a composition body or a loop without caching.
  - SmartDateParser.kt: On-device date detector supporting BS/AD, Devanagari numerals, and text month formats.
- Homescreen Widget: NepToolsDateWidgetProvider.kt (RemoteViews widget for today's BS date, tithi, and festive events with automatic midnight rollover).
- User Calendar Events: UserEventManager.kt (custom events, notes, and local alarm notifications on any BS date).
- Offline Data Backup & Restore: BackupManager.kt (local JSON export/import of habits, subscriptions, notes, and events via SAF).
- Habit Tracker: 52-week contribution heatmap, dual BS/AD monthly view, streaks, numeric and timer targets. Streak arithmetic lives in the pure, unit-tested `HabitStreakCalculator.kt`; `HabitRepository` holds a memoized date-keyed log index because the log blob is keyed by habit, not date.
- Subscription Tracker: Multi-currency normalization (NPR, USD, INR, EUR, GBP) to monthly NPR, recurring bill alerts.
- In-App Updater: GitHub Releases API + web redirect fallback, 32KB streaming buffer, persistent APK disk cache, SHA-256 and PackageArchiveInfo verification.
- Utilities & Hardware: DecibelMeterEngine (dBA sound level), SpyCameraDetectorEngine (EMF sniffer & strobe), CompassEngine, BubbleLevelEngine, LanDropServer, RadioService, ImageCompressor, PdfDocument converter.

---

## 4. Multi-Layer Security Architecture

Full detail lives in agent.md. The summary:

1. Layer 1 (R8 / ProGuard): Shrinking, renaming, and complete stripping of android.util.Log calls. Several packages are deliberately kept unrenamed (see agent.md for the list and why), so obfuscation is partial by design, not by accident.
2. Layer 2 (RASP Engine, advisory only): TracerPid debugger check, /proc/self/maps hooking scan (Frida, Xposed, Substrate), localhost port probes, SU/root detection, signing-certificate SHA-256 allow-list. Invoked from PatroApp.onCreate and surfaced in Settings under "Security & Integrity". Rooted devices are reported, never terminated.
3. Layer 3 (Native C++): Built via externalNativeBuild (CMake, NDK 26). Dynamic JNI registration in JNI_OnLoad (no Java_ exports), symbol stripping (-fvisibility=hidden, -Wl,--strip-all), ptrace(PTRACE_TRACEME) tracer check. Holds no secrets: anything compiled into an APK is extractable, so vault keys come only from the master password plus a random salt.
4. Layer 4 (Signing & Update Verification): Release build signed from keystore.properties (see RELEASE_SIGNING.md). Pre-install requires a published SHA-256 and a package-name identity match; without a checksum the updater refuses to auto-install.

---

## 5. Directory Layout

```
app/src/main/
├── cpp/                             # Native security, CMakeLists.txt, native-lib.cpp
├── java/com/neptools/app/
│   ├── MainActivity.kt              # Single activity entry point
│   ├── PatroApp.kt                  # Application class
│   ├── astrology/                   # Kundali, Dasha, Gochar, Guna Milan
│   ├── core/                        # Engines: calendar, habit, vault, updater, radio, server, security, widget, backup
│   └── ui/                          # screens/, components/, icons/ (PIcons), navigation/ (PatroNavHost)
└── res/                             # Drawables, launcher icons, widget layouts, XML resources
```

---

## 6. Build & Run Commands

- Release signing: `keystore.properties` at the repo root plus `release.keystore` (both gitignored). Procedure, fingerprint, and the one-time user migration are in RELEASE_SIGNING.md. `assembleRelease` **fails** if they are absent rather than falling back to the debug key.
- Unit tests: `.\gradlew.bat testDebugUnitTest`
- Lint: `.\gradlew.bat lintDebug`
- Debug Build: `.\gradlew.bat assembleDebug`
- Install Debug APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r -d app\build\outputs\apk\debug\app-debug.apk`
- Release Build (R8 minified): `.\gradlew.bat assembleRelease`
- Install Release APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\app-release.apk`
- Launch Release App: `& "D:\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.neptools.app -c android.intent.category.LAUNCHER 1`
- Launch Debug App: same command with `-p com.neptools.app.debug`. The debug build type carries `applicationIdSuffix = ".debug"` so debug and release install side by side instead of forcing a full uninstall on every swap.

---

## 7. Status & Recent Changes

- Status: v2.9.0 (versionCode 49) — Event Notification Fidelity, Reboot Alarm Recovery, Restore Cache Invalidation & Widget Sync.
- Last Updated: September 29, 2026
- Verification: 50/50 unit tests pass, assembleRelease signed build verified (SHA-256: 226102D88C38D43C1688A8335E702F831BE7E83F704D8D69A747959801ED8D66).
- Recent Updates:
  - v2.9.0:
    - Event Reminder Fidelity & IDs (Reminder.kt, UserEventManager.kt): Surfaced event title, note, and day deep-link in notifications; assigned distinct IDs to prevent collisions.
    - Reboot & Restore Alarm Recovery (BootCompletedReceiver.kt, UserEventManager.kt, BackupManager.kt): Added `rescheduleAll` restoring event reminders on boot and backup import.
    - Cache & Preference Resiliency (HabitRepository.kt, Theme.kt): Invalidated `logsByDateCache` on backup restore; restored persisted `"np_digits"` setting on cold start.
    - Midnight Widget Synchronization (NepaliDateNotificationManager.kt): Dispatched midnight rollover updates to both Date and Month homescreen widgets.
  - v2.8.9: Navigation gate repair (ALL_LITERAL_ROUTES), daily horoscope refresh removal, habit delete cache invalidation, subscription preference key sync.
  - v2.8.8: Habit streak correctness & heatmap indexing, Ekadashi responsiveness with ephemeris cache, lifecycle state collection, System dark mode, WCAG AA contrast, delete dialogs.
  - v2.8.6 - v2.8.7: Compose clipboard modernization, branding watermark purge in generated PDFs and shares, unbranded intent sharing.
  - v2.8.4 - v2.8.5: Crash-safe atomic file IO, social card generator, automated sacred tithi alerts, subscription presets, authentic sunrise Udaya Tithi.
  - v2.7.0 - v2.8.3: BS 2075-2085 festival deduplication, Govt templates, QR enhancements, bubble level, loan EMI, high-precision ephemeris.


