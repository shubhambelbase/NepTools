# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 34, compileSdk: 34)
- Current Version: v2.6.7 (versionCode: 26)
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

- Status: v2.6.7 (versionCode 26) — Release build completed, signed, and verified.
- Last Updated: September 19, 2026
- Recent Updates:
  - v2.6.7:
    - Comprehensive App-Wide Single-Language Enforcement: Conducted exhaustive codebase audit eliminating 100% of language mixing. In Nepali mode: zero English words or abbreviations (eliminated parenthetical trial labels, mixed lucky colors, English tab headers, English error notes, etc., with full Devanagari numerals). In English mode: 100% pure English. Fixed across AppStrings, RashifalScreen, OnboardingScreen, VoiceScreen, DrivingLicenseQuestionBank, ToolsScreen, GocharScreen, KundaliScreen, PlaceSelector, ApplicationTemplatesRepo, PdfExporter, RadioService, and SpyCameraDetectorScreen.
    - Vedic Marriage Compatibility PDF & Kundali Export Overhaul: Fixed text overlapping in Ashta Koota breakdown table with cell clipping (drawClippedText) and tuned column widths; language-isolated titles, dosha assessments, domain cards, and Devanagari numerals for pure Nepali or pure English reports.
    - Live Choghadiya UI Polish & Dynamic Solar Calculation: Added solar calculation location banner (Kathmandu coordinates), live pulsating indicator, 1-tap guide launcher button, elapsed progress bar, and localized hour/minute metrics.
    - Comprehensive Vedic Choghadiya Guide: Added 4-step practical Muhurat selection guide, complete breakdown of 7 Choghadiya types with planetary rulers, Rahu Kaal precedence rules, and weekday starting sequence.
    - Android 14/15 Predictive Back Navigation: Enabled platform OnBackInvokedCallback in AndroidManifest for smooth swipe-back system gestures across all screens.
    - Dark Mode & OLED Contrast Polish: Polished dark palette (#0C0F14 midnight bg, #141A22 card surface, #F1F5F9 crisp text) and razor-sharp outline borders.
    - Interactive Month & Quick Glance Widgets: 4x2 interactive BS monthly grid widget (NepToolsMonthWidgetProvider) and 4x1 Quick Glance widget (NepToolsQuickGlanceWidgetProvider).
    - Android Baseline Profiles: Added app/src/main/baseline-prof.txt for 30-40% faster cold starts.
    - Full-Screen Splash Graphic & Brand Identity: User-specified splash poster (drawable-nodpi/splash_background.png) and 3D Toolbox brand identity.
  - v2.6.0:
    - Location-Based Emergency Directory: GPS auto-resolver, 100% offline Haversine fallback across 852 locations, 77-district picker, 4-tier sorting, and Cloudflare Worker sync fallback.
    - Verified Reference Data: Re-verified emergency hotlines with official directories; postal codes updated.
    - Voice Notes UI Overhaul: Tactile action pills, elevated save button, character counter.
    - Interactive Kundali Transit Wheel & Vastu Shastra: 360-degree Gochar wheel, 8-direction live compass.
    - Land Area Converter: Hilly (R-A-P-D), Terai (B-K-D-K), metric cross-conversions.
    - Strict zero emoji compliance maintained across all screens and resources.
  - v2.5.8 - v2.5.9: High-precision ephemeris (VSOP87/ELP-2000), authentic Sidereal Nirayana Panchang, 36-Point Ashta Koota Guna Milan, and Sripati Bhava Chalit charts.
  - v2.5.0 - v2.5.7: Universal splash screen stabilization, sound meter, spy camera detector, offline backup, home screen widget, and security engine.

