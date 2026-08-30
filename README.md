# NepTools — Modern Nepali Calendar & Smart Tools Suite

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0%2B)-brightgreen.svg?logo=android)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09.00-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/App%20Version-v0.2.0%20(Build%202)-orange.svg)]()

**NepTools** is a modern, high-performance, offline-first Android calendar and daily utility application crafted in Kotlin and Jetpack Compose. It combines centuries of traditional Nepali astronomical wisdom with cutting-edge mobile architecture, sleek dark-mode aesthetics, and a comprehensive suite of daily tools.

---

## 📱 App Version Information

| Attribute | Specification |
|---|---|
| **Package Name** | `com.neptools.app` |
| **Current Version Name** | `1.0` (`v1.0`) |
| **Current Version Code** | `1` |
| **Minimum Android Version** | Android 8.0 Oreo (API Level 26) |
| **Target Android Version** | Android 14 UpsideDownCake (API Level 34) |
| **Design Language** | **Newari Ink** Material 3 Adaptive Theme (Light & OLED Dark) |

---

## 🌟 Core Features & Tool Suite

### 1. 📅 **Bikram Sambat & Gregorian Calendar Engine**
* **BS ↔ AD Conversion**: Accurate offline bi-directional conversion spanning BS 1975 to BS 2099.
* **Panchang & Astronomical Data**: Daily Tithi, Nakshatra, Sunrise/Sunset, Rahu Kaal, and Yamaganda.
* **Festivals & Public Holidays**: Comprehensive list of Nepali national festivals, bank holidays, and cultural events.
* **Dual Monthly Matrix**: Seamlessly view both BS and AD dates with English and Devanagari numerals.

### 2. 🔥 **Habit Tracker (बानी ट्र्याकर)**
* **52-Week Year Matrix**: GitHub-style horizontal contribution matrix displaying yearly consistency at a glance.
* **Dual BS/AD Heatmap**: Real-time habit completion grid indexed by Nepali months and Gregorian dates.
* **Streak Analytics**: Live calculation of current streak, longest streak, completion rate, and active days.
* **Flexible Habit Types**: Boolean checkboxes, numeric targets (e.g. 2L Water, 10 Pages), and focus timers.

### 3. 💳 **Subscription & Bill Tracker (सदस्यता तथा बिल ट्र्याकर)**
* **Multi-Currency Normalization**: Track subscriptions in NPR (`रू`), USD (`$`), INR (`₹`), EUR (`€`), and GBP (`£`) with automatic NPR monthly conversion.
* **Recurring Billing Cycles**: Weekly, Monthly, Quarterly, Semi-Annual, and Yearly renewal support.
* **Category Breakdown**: High-contrast distributions across Media, Internet, Utilities, Rent, AI, and Fitness.
* **Renewal Countdowns**: Urgent upcoming alerts (e.g., *Due Today*, *In 3 days*) with one-tap *"Paid ✓"* date advancement.

### 4. 🚀 **In-App GitHub Updater (निःशुल्क तथा प्राइभेट अपडेटर)**
* **Private & Public GitHub Releases Support**: Seamlessly checks, downloads, and updates the app directly from your GitHub repository releases.
* **Personal Access Token (PAT)**: Built-in support for secure private repository updates with fine-grained tokens.
* **Background Streaming**: Live download progress bar with byte counts and download speeds.
* **One-Tap Package Installer**: Integrated with Android `FileProvider` and `REQUEST_INSTALL_PACKAGES` to launch instant APK installation.

### 5. 🌌 **Vedic Astrology & Jyotish Suite**
* **Kundali Generator**: Traditional North and South Indian style birth charts.
* **Vimshottari Dasha Engine**: Mahadasha and Antardasha timeline calculations.
* **Gochar (Planetary Transits)**: Current celestial transit positions and effects.
* **Guna Milan (36-Point Compatibility)**: Vedic horoscope matching for marriages.
* **Muhurat Finder**: Auspicious timings for Pasni, Bratabandha, Bibaha, and Griha Pravesh.

### 6. 🛠️ **Smart Utility & Calculator Suite**
* **Loan EMI & Fixed Deposit Calculator**: Monthly repayment schedules and interest breakdown.
* **Forex Currency Converter**: Real-time Nepal Rastra Bank exchange rates.
* **Land Area Converter**: Nepali units (*Ropani-Aana-Paisa-Dam* and *Bigha-Katha-Dhur*) to Square Feet and Meters.
* **Nepali Number to Words & Cheque Writer**: Instant official word conversion for bank cheques.
* **Password Vault & Biometric Gate**: AES-256 encrypted local password and note storage.
* **File & Image Tools**: Format conversion, image compression, and LAN Drop file sharing.
* **Sensors**: Bubble Level, Compass, Speed Test, and Voice Notes.

---

## 🏗️ Project Architecture

The project follows clean architecture principles with single-activity Jetpack Compose navigation:

```
com.neptools.app/
├── core/
│   ├── calendar/          # Pure Kotlin BS Calendar engine, NepaliDate, and Panchang
│   ├── data/              # Offline JSON datasets (PatroRepo, Festivals, Ekadashi)
│   ├── habit/             # Habit models, streak engine, and JSON persistence
│   ├── subscription/      # Subscription models, currency normalizer, and expense engine
│   ├── updater/           # GitHub Releases API client, downloader, and FileProvider installer
│   ├── vault/             # Encrypted password store & Biometrics
│   ├── astrology/         # Ephemeris, Kundali chart generator, Dasha & Gochar
│   └── network/           # Forex, Fuel, Weather, and Radio streaming services
├── ui/
│   ├── navigation/        # PatroNavHost, Routes, and Bottom Navigation Bar
│   ├── screens/           # Jetpack Compose UI Screens
│   │   ├── HabitTrackerScreen.kt
│   │   ├── SubscriptionTrackerScreen.kt
│   │   ├── AppUpdaterScreen.kt
│   │   ├── CalendarScreen.kt
│   │   ├── ToolsScreen.kt
│   │   └── SettingsScreen.kt
│   ├── components/        # Reusable Compose UI components & Devanagari number formatters
│   ├── icons/             # Custom lightweight Material ImageVector icons (PIcons)
│   └── theme/             # Newari Ink Theme tokens, Typography, and ColorSchemes
```

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1+) or newer
* **Java Development Kit (JDK)**: OpenJDK 17
* **Android SDK**: API Level 34 (Android 14)

### Building from Source

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/shubhambelbase/NepTools.git
   cd NepTools
   ```

2. **Assemble Debug APK**:
   ```powershell
   ./gradlew :app:assembleDebug
   ```

3. **Install on Connected Android Device**:
   ```powershell
   ./gradlew :app:installDebug
   ```

4. **Run Unit Tests**:
   ```powershell
   ./gradlew :app:testDebugUnitTest
   ```

---

## 📦 In-App GitHub Releases Workflow

To distribute updates directly to app users:

1. **Bump Version Code**:
   In `app/build.gradle.kts`, increment `versionCode` (e.g. `3`) and `versionName` (e.g. `"0.3.0"`).
2. **Build Release or Debug APK**:
   ```powershell
   ./gradlew :app:assembleDebug
   ```
3. **Publish a Release via GitHub CLI**:
   ```bash
   gh release create v0.3.0 app/build/outputs/apk/debug/app-debug.apk \
     --title "v0.3.0 - Habit Tracker & Subscription Enhancements" \
     --notes "• Clean Habit Tracker UI\n• Dark mode contrast optimizations\n• In-app GitHub update installer"
   ```
4. **App Receives Update**:
   Users open **App Updates** (`Tools` → `App Updates`) or receive a prompt, tap **"Download & Install"**, and the new APK updates automatically!

---

## 📄 License & Attribution

Designed and developed with ❤️ for Nepal.  
*Copyright © 2026 Shubham Belbase. All rights reserved.*
