# NepTools - Modern Nepali Calendar & Smart Utility Suite

[![Version](https://img.shields.io/badge/Version-v2.6.2-orange.svg)](https://github.com/shubhambelbase/NepTools/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://github.com/shubhambelbase/NepTools)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](https://github.com/shubhambelbase/NepTools)

NepTools is an all-in-one, modern Nepali calendar and smart utility application designed for Android. Combining authentic astronomical calculations with a modern touch-first design, NepTools provides everything you need in daily life - from Bikram Sambat dates, festival alerts, and panchang to finance calculators, habit tracking, encrypted backup, land measurements, Vastu Shastra directions, and Vedic astrological insights.

---

## Download & Install

Download the latest official production APK directly from the Releases page:

**[Download Latest NepTools APK (v2.6.3)](https://github.com/shubhambelbase/NepTools/releases/latest)**

1. Download the `app-release.apk` file from the latest release.
2. Open the file on your Android device (enable "Install unknown apps" if prompted).
3. Tap Install and launch NepTools.

> **Upgrading from v2.6.1 or older?** NepTools is now signed with a dedicated release key, so Android treats it as a different signer. One-time step: export a backup from **Settings**, uninstall the old build, install v2.6.2, then restore your backup.

---

## Core Features & Tools

### 1. Bikram Sambat (BS) Calendar & Panchang
- Accurate Bikram Sambat date engine spanning BS 1970 to BS 2100+.
- Daily Panchang: Tithi, Nakshatra, Yoga, Karna, Sunrise, Sunset, Rahu Kaal, and Yamaganda timings.
- Fast BS to AD and AD to BS bi-directional date converter with smart clipboard detection.
- Comprehensive calendar of Nepali holidays, government observances, and cultural festivals.
- Clean Android home screen widget with automated midnight rollover, rendering off the main thread to keep your launcher smooth.

### 2. Vedic Astrology & Jyotish Suite
- Birth Chart (Janma Kundali): Authentic North Indian and South Indian chart calculations with Sripati Bhava Chalit house cusps.
- Vimshottari Dasha: Complete 120-year planetary cycle breakdown with Mahadasha and Antardasha periods.
- Interactive Gochar Transit Wheel: 360-degree dual-ring planetary wheel comparing real-time planetary transits against the birth chart, highlighting active planetary aspects, conjunctions, and retrograde status with an on-device ephemeris time controller.
- Ashta Koota Guna Milan: 36-point marital compatibility engine based on classical Brihat Parashara Hora Shastra principles.
- Auspicious Muhurat Finder: Favorable dates and times for weddings, Bratabandha, Pasni, and Griha Pravesh.

### 3. Vastu Shastra Compass
- Classical 8-direction live compass dial overlay featuring traditional deities, elements, and cardinal headings.
- Nepali home room placement guide (ideal locations for prayer rooms, kitchens, bedrooms, entrances, cash lockers, and study areas).
- Live room suitability checker with instant directional verdicts.

### 4. Land Area Converter
- Official Nepal Survey Department standards for Hilly and Valley system (Ropani, Aana, Paisa, Daam) and Terai system (Bigha, Katha, Dhur, Kanwa).
- Cross-conversion to International metric and imperial units (Square Feet, Square Metres, Acres, Hectares).
- Parcel Arithmetic: Multi-parcel addition and deduction calculation with net remaining land computation.

### 5. Habit & Goal Tracker
- 52-week horizontal momentum heatmap grid.
- Dual BS and AD monthly views with streak analytics and completion rates.
- Flexible targets: Check-ins, numeric metrics (water, reading), and timed sessions.

### 6. Subscription & Bill Manager
- Multi-currency tracking with automatic normalization to Nepali Rupees (NPR).
- Flexible billing cycles (monthly, quarterly, yearly).
- Due date reminders and payment status tracking.

### 7. Encrypted Vault & Offline Backup
- AES-256 encrypted local vault with biometric fingerprint and PIN protection.
- Privacy-first clipboard: vault copies are flagged sensitive on Android 13+, hiding them from the clipboard preview overlay and history, and the vault screen is excluded from screenshots and the recents overview.
- Portable JSON backup and restore for habits, subscriptions, notes, and personal events via Android Storage Access Framework (SAF).
- 100% offline-first architecture with zero tracking, zero telemetry, and zero ads.

### 8. Smart Utilities & Daily Tools
- Bilingual Voice Notes with speech-to-text transcription.
- Calibrated Decibel Sound Level Meter with real-time waveform oscillograph.
- Spy Camera & Bug Detector with EMF magnetic field sniffer and optical reflection strobe.
- Live Forex rates from Nepal Rastra Bank and fuel prices from Nepal Oil Corporation.
- Kalimati daily vegetable and fruit market wholesale price tracker.
- Online Nepali FM Radio streaming service.
- LanDrop: share files over Wi-Fi to any browser on the same network, protected by a per-session 6-digit access code with automatic idle shutdown.
- Smart utilities: Compass, Bubble Level, Image Compressor, PDF Document Converter, Speed Test, Loan EMI Calculator, and Decision Maker.

### 9. Location-Based Emergency Directory & Edge Sync
- Automatic on-device GPS location detection with 100% offline Haversine fallback across 852 locations in Nepal.
- 77-district and 7-province manual selector.
- 61 verified emergency contacts (police control, ambulance, fire rescue, blood banks, hospitals, and child/women hotlines).
- 4-tier dynamic prioritization surfacing local district services at the top.
- Dual-cloud edge synchronization (Cloudflare Worker edge API + GitHub raw CDN fallback) with offline caching.

### 10. Security & Integrity
- Official release signing: every distributed APK is signed with a dedicated RSA-4096 release key.
- Live Security & Integrity panel in Settings showing signature, installer, and tamper status.
- Checksum-enforced in-app updater: releases without a published SHA-256 checksum are never auto-installed, and every download is integrity-verified before installation.

---

## Requirements

- Android 8.0 (API Level 26) or higher.
- Storage: Approximately 15 MB free space.
- Network: 100% offline functional (Internet optional for live Forex, Kalimati rates, and Radio streaming).

---

## Updates

NepTools features an integrated offline-friendly update checker with checksum-verified installation. You can check for new releases anytime from **Settings -> About -> App Updates**.

---

## License & Attribution

Designed and developed by Shubham Belbase.  
Copyright 2026 Shubham Belbase. All rights reserved.  
Distributed as compiled application packages only.
