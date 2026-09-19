# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 34, compileSdk: 34)
- Current Version: v2.6.9 (versionCode: 28)
- Last Updated: September 19, 2026
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

---

## 3. Technology Stack & Key Engines

- UI & Theme: Jetpack Compose + Material 3. Custom NepToolsTheme (Newari Ink / Rice Paper palette) with light and dark mode.
- Navigation: Compose Navigation (PatroNavHost.kt) with animated transitions.
- Calendar & Astronomy:
  - BsCalendarEngine.kt: Bikram Sambat date math (BS 1970 to BS 2100+).
  - SolarCalc.kt: NOAA solar engine for live sunrise, sunset, and Rahu Kaal from GPS or selected district coordinates.
  - PanchangCalc.kt & DynamicFestivalEngine.kt: Tithi, Nakshatra, Yoga, and festive observances.
  - SmartDateParser.kt: On-device date detector supporting BS/AD, Devanagari numerals, and text month formats.
- Homescreen Widget: NepToolsDateWidgetProvider.kt (RemoteViews widget for today's BS date, tithi, and festive events with automatic midnight rollover).
- User Calendar Events: UserEventManager.kt (custom events, notes, and local alarm notifications on any BS date).
- Offline Data Backup & Restore: BackupManager.kt (local JSON export/import of habits, subscriptions, notes, and events via SAF).
- Habit Tracker: 52-week contribution heatmap, dual BS/AD monthly view, streaks, numeric and timer targets.
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

- Release signing: `keystore.properties` at the repo root plus `release.keystore` (both gitignored). Procedure, fingerprint, and the one-time user migration are in RELEASE_SIGNING.md. A release build without them falls back to the public debug key and warns loudly.
- Unit tests: `.\gradlew.bat testDebugUnitTest`
- Lint: `.\gradlew.bat lintDebug`
- Debug Build: `.\gradlew.bat assembleDebug`
- Install Debug APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r -d app\build\outputs\apk\debug\app-debug.apk`
- Release Build (R8 minified): `.\gradlew.bat assembleRelease`
- Install Release APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\app-release.apk`
- Launch App: `& "D:\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.neptools.app -c android.intent.category.LAUNCHER 1`

---

## 7. Status & Recent Changes

- Status: v2.6.9 (versionCode 28) — Launcher shortcuts, tactile haptics, 1-tap utility sharing, and tools search/recents implemented.
- Last Updated: September 19, 2026
- Recent Updates:
  - v2.6.9:
    - Launcher Quick App Shortcuts: Configured Android dynamic shortcuts (shortcuts.xml) for Today's Calendar, Date Converter, Emergency Numbers, and QR Scanner with vector drawables and cold-start route replay in AppNavigator.
    - Tactile Haptic Micro-Interactions: Added physics-based haptic feedback in DecisionMakerScreen (slice-crossing deceleration ticks on the wheel, crisp snap on coin landing impact, and progressive bounce bumps on dice rolls).
    - 1-Tap Utility Share Summaries: Added instant calculation sharing for Loan EMI & FD (LoanEmiScreen), Land Area breakdowns across Hilly (R-A-P-D), Terai (B-K-D-K), and Metric units (LandConverterScreen), and group bill splits (BillSplitterScreen) formatted with Devanagari numerals.
    - Tools Section Discoverability & Instant Search: Added live search bar with bilingual matching and keyword alias mapping (TOOL_SEARCH_KEYWORDS), search result count, and contextual empty state.
    - Recently Used Tools Section: Built RecentToolsManager (SharedPreferences-backed, reactive mutableStateListOf) showing top-5 recently used tools with 1-tap clear action.
    - Robust In-App Update Checksum Extraction: Updated SHA-256 regex parser in GitHubUpdateManager to accept all markdown formatting variations.
  - v2.6.7:
    - Comprehensive App-Wide Single-Language Enforcement: Pure Devanagari in Nepali mode; 100% English in English mode.
    - Vedic Marriage Compatibility PDF & Kundali Export Overhaul: Cell clipping, tuned column widths, and language-isolated tables.
    - Live Choghadiya UI & Vedic Choghadiya Guide: Dynamic solar calculations, pulsating indicator, and 4-step selection guide.
    - Android 14/15 Predictive Back Navigation & Dark Mode Polish: Enabled OnBackInvokedCallback; midnight OLED palette.
    - Baseline Profiles & Brand Assets: Cold start optimization and 3D Toolbox splash branding.
  - v2.6.0: Location-based emergency directory (offline Haversine fallback), Kundali Gochar wheel, Vastu compass, Land Area converter.
  - v2.5.0 - v2.5.9: High-precision ephemeris, 36-Point Guna Milan, sound meter, spy camera detector, offline backup, widgets, and security engine.


