# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 36, compileSdk: 36)
- Current Version: v2.8.4 (versionCode: 43)
- Last Updated: September 22, 2026
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

- Status: v2.8.4 (versionCode 43) — Calendar Holiday & Udaya Tithi Accuracy Overhaul.
- Last Updated: September 22, 2026
- Recent Updates:
  - v2.8.4:
    - Calendar Holiday vs Festival Distinction: Fixed calendar cell styling bug where ordinary festivals erroneously highlighted days in holiday red. Only official public holidays (Saturdays and gazetted holidays) are red; ordinary working festivals are shown in subtle secondary/teal.
    - Authentic Udaya Tithi at Local Sunrise: Updated PanchangCalc.compute() to sample lunar elongation at authentic local sunrise via SolarCalc, matching canonical Vedic Panchang principles.
    - Cell Grid Tithi Display: Injected localized Tithi on every calendar grid day cell. Added clear bottom legend distinguishing Public Holidays (red), Festivals (teal), and Today.
    - Hamro Patro & Gazette Alignment: Cross-verified all months for BS 2081 through 2085. Aligned multi-day Tihar, Dashain Dwadashi, Eid-ul-Fitr, Bakrid, Bhoto Jatra, and purged incorrect holiday flags on regular working festivals.
  - v2.8.3:
    - Full BS 2083 Month-by-Month Verification: Verified all 12 months of BS 2083 against Hamro Patro and Nepal Panchanga Nirnayak Vikas Samiti. Aligned Padmini & Parama Ekadashis, restored Ashadh 15, aligned Tihar sequence, and added Chaite Dashain & Ram Navami in 2084.
  - v2.8.2:
    - Full 11-Year Festival Deduplication & Gazette Holiday Enforcement (festivals_sample.json): Eliminated all same-day substring and constituent overlaps across 132 months. Strictly aligned all publicHoliday flags with the official Nepal Gazette. 100% Devanagari-free English translations.
  - v2.8.1:
    - 11-Year Official Panchanga Dataset Forensic Audit & Sanitization (festivals_sample.json): Conducted comprehensive audit across all 132 months (BS 2075-2085). Decoded 24 mojibake encoding corruptions to clean Devanagari, purged all scraped gazette fragments/noise, unified same-day duplicates, achieved 100% English translation coverage, and injected full verified festival calendar for BS 2084 and 2085.
  - v2.8.0:
    - Bikram Sambat Lunar Festival Engine Overhaul (PanchangCalc.kt, DynamicFestivalEngine.kt): Re-architected dynamic festival determination from simplistic solar month checks to authentic astronomical Lunar Masa indexing (Amanta / Purnimanta via sidereal Sun position at New Moon). Resolves Adhik Maas years (e.g. 2080, 2083 BS) where major festivals like Dashain, Tihar, Buddha Jayanti, Janai Purnima were previously placed in wrong months.
    - Screen-Reader Accessibility Fix (CalendarCells.kt): Cleaned TalkBack content descriptions to speak localized festival names properly instead of printing raw object instances.
  - v2.7.9:
    - Universal Notification Deep Linking: Tapping weather, subscription, habit, or calendar alerts routes directly to the corresponding tool via AppNavigator.
    - 3D Brand Notification Asset Overhaul: Created crisp multi-density ic_notification_large.png circular badge. Migrated notification channels to v4/v2 and invalidated stale launcher icon caches.
  - v2.7.8:
    - Official Nepal Govt Templates Compliance (ApplicationTemplatesRepo.kt): Standardized all 9 templates according to the Local Government Operation Act 2074, Nepal Citizenship Act 2063 & Rules, Muluki Civil Code 2074, and Vital Registration Act 2076. Fixed citizenship date of birth bug, added mandatory 3-generation genealogy (बाजेको नाम), added statutory declarations, comprehensive document checklists, and added Relationship Verification (Nata Pramanit) template.
  - v2.7.7:
    - Govt Templates Modernization: Compact inline header with verified badge, category chips, inline selectable cards.
    - QR Code Generator Enhancements: Genuine scannable ZXing QR generation, standard eSewa JSON schema, single Save QR action.
    - Bubble Level Micro-Animations: Spring physics, haptics on level lock (<0.5 deg), theme-adaptive concentric rings.
    - Fuel Calculator Streamlining: Clean two-way converter, 1-tap quick presets, dedicated Trip Cost Estimator.
  - v2.7.0 - v2.7.6: Date converter micro-animations, subscription & habit empty state cleanup, category filter chips, atomic JSON persistence, accessibility semantics, high-precision ephemeris, 36-Point Guna Milan, and security engine.


