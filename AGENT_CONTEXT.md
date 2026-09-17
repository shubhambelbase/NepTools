# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 34, compileSdk: 34)
- Current Version: v2.6.0 (versionCode: 19)
- Last Updated: September 17, 2026
- Languages: Kotlin (JVM 17) + C++20 for native security
- UI Toolkit: 100% Jetpack Compose (Material 3) with Compose BOM
- Creator: Shubham Belbase
- Distribution: Public releases at https://github.com/shubhambelbase/NepTools

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

1. Layer 1 (R8 / ProGuard): Class repackaging into root (''), complete stripping of android.util.Log calls, source file obfuscation.
2. Layer 2 (RASP Engine): Active debugger check (TracerPid procfs), /proc/self/maps scanning (Frida, Xposed, Substrate), localhost port probing, SU/root binary detection, certificate SHA-256 fingerprint pinning.
3. Layer 3 (Native C++): Dynamic JNI registration in JNI_OnLoad (no Java_ exports), symbol stripping (-fvisibility=hidden, -Wl,--strip-all), native ptrace(PTRACE_TRACEME) anti-debugging, XOR-masked secrets.
4. Layer 4 (Update Verification): Pre-install SHA-256 hash checking and package name identity validation.

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

- Debug Build: `.\gradlew.bat assembleDebug`
- Install Debug APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r -d app\build\outputs\apk\debug\app-debug.apk`
- Release Build (R8 minified): `.\gradlew.bat assembleRelease`
- Install Release APK: `& "D:\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\app-release.apk`
- Launch App: `& "D:\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.neptools.app -c android.intent.category.LAUNCHER 1`

---

## 7. Status & Recent Changes

- Status: v2.6.0 (versionCode 19).
- Last Updated: September 17, 2026
- Recent Updates:
  - v2.6.0:
    - Voice Notes UI Overhaul (VoiceScreen.kt): Replaced clunky OutlinedButton row with tactile VoiceActionButton pills (spring press bounce, haptics, in-place animated green checkmark on copy, soft rose container with 3.5s double-tap confirmation on clear for long notes), elevated full-width VoiceSaveButton, and upgraded card header with character badge.
    - Category Reorganization (ToolsScreen.kt): Moved Kundali & Vedic (Routes.ASTRO) from Daily Services into the top of Panchang & Jyotish category.
    - Interactive Kundali Transit Wheel (TransitWheelView.kt & GocharScreen.kt): 360-degree dual-ring planetary wheel comparing live Gochar transits against birth chart, with active aspect/conjunction rays, retrograde status, and on-device time controller.
    - Vastu Shastra Compass (VastuEngine.kt & VastuCompassScreen.kt): Classical 8-direction live compass overlay with Sanskrit deities, elements, room suitability verifier, and Nepali house architectural guide.
    - Land Area Converter (LandConverter.kt & LandConverterScreen.kt): Official Nepal Survey Department cross-converter for Hilly (R-A-P-D), Terai (B-K-D-K), Metric/Imperial, and parcel arithmetic (+/-).
    - Bilingual English & Nepali Localization: Full English mode support with authentic bilingual pairings (e.g., Ropani/Aana, Bigha/Katha, Ishanya/Agni, Vakri Rx, Janma/Gochar) across Land Converter, Vastu Compass, and Gochar Wheel for clear comprehension.
    - Architecture, Backup & Polish: Central Bank Forex & NOC Fuel live pricing; passed HighPrecisionEphemeris to AstroRepo; expanded BackupManager to Kundali profiles and favorite tools; strict zero emoji compliance.
  - v2.5.9:
    - Authentic 36-Point Ashta Koota Guna Milan Engine (AshtakootaGunaMilan.kt): Implemented classical Brihat Parashara Hora Shastra tables for all 8 Kootas (Nadi, Bhakoot, Gana, Maitri, Yoni, Tara, Vashya, Varna).
    - Upgraded GunaMilanScreen.kt: Human-friendly compatibility overview with percentage, plain-language marital guidance, 3 critical dosha indicators (Nadi, Bhakoot, Gana), 4 life-domain scorecards, 8 expandable Guna accordions, and 1-tap auto-fill from saved birth chart.
  - v2.5.8:
    - High-Precision Astrology Engine: Embedded pure Kotlin VSOP87 perturbation theory, ELP-2000 lunar theory, and NOVAS C 3.1 sidereal time algorithms (100% offline).
    - Fixed Daily Panchang calculation (PanchangCalc.kt) to compute authentic Sidereal (Nirayana) coordinates, eliminating the 2 Nakshatra and 4 Yoga discrepancy.
    - Added true Lunar Node calculation with evection oscillations (+-1.75 degrees).
    - Implemented authentic Sripati Bhava Chalit house cusps and interactive Rashi D1 vs Bhava Chalit chart toggle in KundaliScreen.
    - Fixed UI text and component overlap glitch across accordion cards (ExpandablePlanet, showGuide, and showTimeline) by refactoring SoftCard container to Column and refining indicator dividers and chevrons.
    - Added pre-1986 Nepal timezone auto-correction (UTC+5:30 IST).
  - v2.5.7:
    - Removed offline & private sub-badge from splash screen per user specification.
    - Optimized splash duration to 700ms with 750ms internal and 850ms native Looper watchdogs.
    - Multi-route fail-safe entry: BackHandler instant skip, full-screen tap dismiss, deep-link bypass, and onResume/onStop lifecycle splash recovery.
  - v2.5.6:
    - Restored authentic signature warm Nepali rice paper and golden sunrise splash screen palette across all system themes.
    - Preserved non-blocking async startup, battery saver fast-path bypass, responsive geometry scaling, and instant tap-to-skip.
  - v2.5.5:
    - Universal splash screen stabilization: non-blocking asynchronous cold start (WorkScheduler and ReminderHelper moved off main thread), zero-scale animator duration and battery saver detection.
    - Dark mode window theme parity (#0D1418 background) and dynamic midnight Himalayan sky splash palette.
    - Responsive splash geometry scaling with BoxWithConstraints and instant tap-to-skip gesture.
  - v2.5.4:
    - Added Dynamic Favorite Tools section at top of ToolsScreen with persistent 1-tap star bookmarking (FavoriteToolsManager).
    - Redesigned Voice Notes: bilingual speech recognition (Nepali ne-NP / English en-US), pulsing mic halo, real-time waveform bars, full transcript editor (copy, clear, save), and saved memos manager.
    - Added Sound Level Meter (AudioRecord dBA measurement, rolling waveform oscillograph, WHO noise safety indicators).
    - Added Spy Camera & Bug Detector with Master Inspection Guide (EMF magnetic sniffer, optical lens reflection strobe, and infrared detection).
    - Added Android Home Screen Widget (Daily Bikram Sambat Date, Tithi & Festival with midnight auto-update).
    - Added Offline Local Data Backup & Restore (export/import habits, subscriptions, notes, events via SAF).
    - Added Custom Calendar Events & Personal Reminders with 8:00 AM notifications and cell dots.
    - Clean tool optimization: Removed BLE Radar tool and Gold & Silver rates per user specifications.
    - Codebase hygiene: Removed Bluetooth permissions, ensured strict zero emoji compliance.
  - v2.5.3: Universal splash screen fix and stability improvements.
  - v2.5.2: Refined Decision Wheel winner outcome card.
  - v2.5.1: Overhauled Decision Wheel, Coin Toss, and Dice Roller tools.
  - v2.5.0: Core bug fixes and performance improvements.
