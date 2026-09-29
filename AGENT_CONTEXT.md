# NepTools Project Context and Architectural Guide

> Mandatory rule for all AI agents: Keep this file updated on every material change. Maintain under 150 lines. No emojis anywhere.

---

## 1. Project Overview & Identity

- App Name: NepTools (strictly "NepTools", never standalone "Nepal Patro")
- Package Name / Application ID: com.neptools.app
- Target Platform: Android (minSdk: 26, targetSdk: 36, compileSdk: 36)
- Current Version: v2.8.9 (versionCode: 48)
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

- Status: v2.8.9 (versionCode 48) — Full Tools Navigation Fix, Rashifal Refresh Removal, Cache Invalidation & Setting Keys.
- Last Updated: September 29, 2026
- Verification: 50/50 unit tests pass, assembleRelease succeeds.
- Recent Updates:
  - v2.8.9:
    - Navigation Gate Repair (PatroNavHost.kt): Replaced overly restrictive 8-route allowlist with `ALL_LITERAL_ROUTES` (50+ registered routes) and parameterized route verification (`isValidRoute`), restoring access to all tools from ToolsScreen and HomeScreen.
    - Daily Horoscope Refresh Removal (RashifalScreen.kt): Removed redundant top bar refresh button and refresh key state; seed now deterministically derives from the selected rashi and current date.
    - Habit Log Cache Invalidation (HabitRepository.kt): Fixed `deleteHabit` failing to invalidate `logsByDateCache`, preventing deleted habits' logs from persisting in memory.
    - Subscription Notification Preference Key Sync (Theme.kt): Resolved key mismatch where `saveSubNotification` wrote `notif_sub` while `ThemePrefs.load` read `notif_subs`.
    - Ekadashi Year-Switch Responsiveness (EkadashiListScreen.kt): Keyed state variables by `selectedYear` for immediate cache resolution and loading state display.
  - v2.8.8:
    - Habit Streak Correctness Fix (HabitRepository.kt, HabitStreakCalculator.kt): Fixed dead ternary and firstStreakBroken flag. Extracted pure `HabitStreakCalculator` with neutral rest days.
    - Habit Heatmap Performance (HabitRepository.kt): Added memoized date-keyed index (`allLogsByDate`) invalidated on write.
    - Ekadashi Screen Responsiveness (EkadashiListScreen.kt): Ephemeris solves offloaded to Dispatchers.Default with ConcurrentHashMap cache.
    - Lifecycle State Collection: Migrated 28 collectAsState() to collectAsStateWithLifecycle().
    - System Dark Mode (Theme.kt, SettingsScreen.kt): Added ThemeMode (System/Light/Dark) resolved via isSystemInDarkTheme.
    - Text Contrast & Container Colors (Color.kt): Faded darkened to #6B6455 (WCAG AA); replaced translucent container with VermilionContainer.
    - Keyboard Insets (PatroNavHost.kt): Applied safeDrawing + imePadding().
    - Destructive Action Confirmation: Added confirmation dialogs for habit and subscription deletion.
    - Deep-Link Validation & Release Signing Hardening: Checked external routes; required release keystore for assembleRelease.
    - Boot Receiver IO (BootCompletedReceiver.kt): Offloaded to Dispatchers.IO via goAsync(). Removed test broadcast actions.
    - Cold-Start Colour Step: Window background matches theme in MainActivity. First-run seeds language from device locale.
    - Removed the unused `CHANGE_WIFI_MULTICAST_STATE` permission.
  - v2.8.7:
    - Clean Graphics & PDF Watermark Purge: Removed promotional footers, "NepTools 100% Ad-Free • Privacy-First", website URLs, and copyright strings from Patro and Rashifal graphic cards (`PatroGraphicGenerator.kt`), bill receipts (`ReceiptGraphicGenerator.kt`), Kundali and Guna Milan astrology reports (`AstroPdfExporter.kt`), and official application letters (`PdfExporter.kt`).
    - Unbranded Social Sharing: Purged promotional branding, extra marketing text, and GitHub links from `ACTION_SEND` intents across card sharing, bill splitters, land converter, and loan EMI calculators.
  - v2.8.6:
    - Compose Clipboard Modernization: Replaced deprecated `LocalClipboardManager` with coroutine-backed `LocalClipboard.current` in `ClipboardExtensions.kt` across 5 screen components (`ConverterScreen.kt`, `EmergencyScreen.kt`, `LandConverterScreen.kt`, `PostalCodeScreen.kt`, `VoiceScreen.kt`), achieving 0 compiler warnings.
    - Studio Commercial Production: Produced sleek 9:16 vertical product showcase video using HyperFrames, Gemini TTS Nepali voiceover, and hardware mockup transitions.
  - v2.8.5:
    - Crash-Safe File IO (SafeFileWriter.kt): Implemented atomic write-flush-sync-rename pattern across all JSON cache stores (NotesStore, ReminderStore, BackupManager, FuelRepo, KalimatiRepo, RatesRepo, WeatherRepo) preventing cache corruption on abrupt process kills.
    - Concurrency & Thread-Safety: Added reentrant lock synchronization in RecentUpdatesManager to protect shared list mutations against background worker interleaving.
    - Main-Thread IO Offloading: Migrated heavy operations (social card rasterization, PDF rendering, Password Vault AES re-encryption, initial repository cache reads) to Dispatchers.IO and Default coroutines.
    - In-App Updater Resiliency (GitHubUpdateManager.kt): Hardened release updater with fallback sha256 checksum resolution via companion asset (app-release.apk.sha256) and web release page parsing to prevent IP rate-limiting errors.
  - v2.8.4:
    - Daily Patro & Rashifal Card Generator (PatroGraphicGenerator.kt): 1080x1440 high-resolution social card generator in Newari Ink / Rice Paper styling with BS/AD dates, Tithi, Nakshatra, Yoga, Sunrise/Sunset, Rahu Kaal, festive banners, and Subhashita blessing with 1-tap WhatsApp/Viber sharing from Home, Day Detail, Rashifal, and Ekadashi screens.
    - Automated Festival & Fasting Reminders (SmartAlertNotificationManager.kt, SacredTithiResolver.kt): Added background notification engine alerting the evening prior for upcoming Ekadashis, Aunsi, Purnima, and major festivals, plus morning Dwadashi Parana timing alerts with Settings and in-screen toggles.
    - Universal Tool Header & Typography Standardization (PatroComponents.kt): Created reusable ToolTopBar unifying all 35+ tool screens with sleek typography (16.5sp bold title, -0.2sp tracking, and 11sp onSurfaceVariant subtitle) alongside 36dp surface pill back actions. Enforced zero emojis across all tools.
    - Subscription Tracker Overhaul (SubscriptionTrackerScreen.kt): Added quick utility presets (NTC, WorldLink, NEA, Khanepani), comprehensive outflow dashboard with category distribution, 1-tap quick paid buttons, and foreign currency NPR conversion estimates.
    - Calendar Holiday vs Festival Distinction: Fixed calendar cell styling bug where ordinary festivals highlighted days in holiday red. Only official public holidays are red; ordinary working festivals are shown in subtle secondary/teal.
    - Authentic Udaya Tithi at Local Sunrise: Updated PanchangCalc.compute() to sample lunar elongation at authentic local sunrise via SolarCalc.
    - Cell Grid Tithi Display & Gazette Alignment: Localized Tithi on every calendar grid day cell and cross-verified BS 2081 through 2085.
  - v2.8.0 - v2.8.3: Full BS 2083 and 11-Year Festival Deduplication (BS 2075-2085) with Gazette holiday enforcement across all 132 months; Bikram Sambat Lunar Festival Engine re-architected to authentic astronomical Lunar Masa indexing; Screen-Reader accessibility fix in CalendarCells.kt. Per-version detail in `release_notes_*.md` and git history.
  - v2.7.0 - v2.7.9: Govt templates modernization against official Acts and Rules, QR code enhancements, notification deep linking, bubble level micro-animations, fuel and date converter refinements, subscription and habit empty state cleanup, high-precision ephemeris.


