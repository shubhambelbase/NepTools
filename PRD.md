# PRD — "Nepal Patro" (working title)
**Lightweight, Offline-First Nepali Calendar & Utility Suite for Android**

| Field | Value |
|---|---|
| Version | 1.0 (Draft) |
| Date | 2026-08-23 |
| Platform | Android 8.0+ (API 26+), Kotlin |
| Model | 100% free resources, serverless, on-device only |

---

## 1. Vision

A single lightweight app that replaces Hamro Patro's daily-use features for Nepal: Bikram Sambat calendar with festivals and holidays, panchang/patro details, rashifal, date conversion, plus practical tools (currency, weather, converters) and on-device AI features powered by ONNX. **No accounts, no servers we own, no tracking — everything works offline by default.**

## 2. Goals

- **G1:** Full BS (Bikram Sambat) calendar 1975–2100 BS with festivals, public holidays, tithi — accurate against published patros.
- **G2:** Works fully offline after first install. Zero backend cost, zero owned infrastructure.
- **G3:** APK < 25 MB base; AI models downloaded on-demand so base stays light.
- **G4:** Cold start < 1s on low-end devices (2GB RAM class).
- **G5:** Only free/public data sources; no paid APIs anywhere.

### Non-goals
- No user accounts / cloud sync / social feed.
- No FM radio, news scraping, or matrimony (Hamro Patro bloat we avoid).
- No iOS in v1.
- Not a substitute for astrological consultation — rashifal is rule-based/generated locally.

## 3. Target Users & Personas

| Persona | Need |
|---|---|
| **Sita, 34, Kathmandu office worker** | Checks today's date/festival, sets reminders for puja/saune sani, converts BS↔AD for forms. |
| **Ram, 21, student** | Currency converter for online shopping, unit converter, age calculator in BS. |
| **Kamala, 58, homemaker** | Large fonts, tithi/nakshatra for fasting dates, rashifal daily. |
| **Bikash, 28, migrant worker in Gulf** | Offline everything (expensive data), NPR↔AED/SAR rates when connected, Nepal time + local time. |

## 4. Features

### 4.1 MVP (v1.0)

#### F1. Nepali Calendar (core)
- Month grid in BS, today highlighted, swipe between months/years (1975–2100 BS).
- Per-day markers: festivals (red), Saturdays (holiday, blue), full/half holidays.
- Event detail sheet: festival name (NP/EN), description, holiday type, tithi.
- Data source: **bundled static dataset** (JSON/Room prepopulate) compiled from public patro data — no API needed, this is deterministic historical/astronomical data.

#### F2. Panchang (Daily Patro)
- Tithi, nakshatra, yoga, karan, vara (weekday), sunrise/sunset/moonrise.
- Computed **locally** via astronomical algorithms (Meeus-based solar/lunar position math — free, no API). Sunrise/sunset via NOAA solar equations using device lat/long or selected city (78 districts preloaded coordinates).
- Rahu kaal, abhijit muhurat windows.

#### F3. Date Converter
- Bidirectional BS ↔ AD converter (type or pick date).
- Also shows corresponding Nepali numerals and weekday.

#### F4. Festival Reminders & Alarms
- Local notifications (WorkManager/AlarmManager) for upcoming festivals, ekadashi, purnima/aunsi.
- Custom reminder on any calendar day ("momo party", "fee deadline").
- Nepali-language notification strings.

#### F5. Currency Converter
- NPR base + ~30 currencies (USD, INR, AED, SAR, QAR, MYR, KRW, JPY, AUD, GBP, EUR...).
- Online: refresh from **open.er-api.com** (free, no key) every 12h via WorkManager; cached in Room.
- Offline: last cached rates + NRB pegged-INR constant fallback (INR 1.60 NPR fixed).
- Clear "rates updated X hours ago" badge.

#### F6. Unit Converter
- Local computation only: length, weight, temperature, area (ropani–aana–paisa–dam! , bigha–kattha–dhur), speed, data.
- Nepali traditional units first-class citizens.

#### F7. Age Calculator (BS)
- Age in years/months/days computed in BS, next-birthday countdown, total days lived.

#### F8. Rashifal (Daily Horoscope)
- 12 rashi, daily text generated **on-device**: seeded from date + rashi → template engine picks from curated sentence banks (~500 curated NP sentences bundled). Deterministic, offline, zero API. (Same trick many patro apps use.)

### 4.2 v1.x (fast follow)

#### F9. Weather (Open-Meteo)
- Current + 7-day forecast for Nepali cities/districts via **Open-Meteo** (free, no API key, no attribution-required tier issues).
- Cache 6h; fully viewable offline with cached data.

#### F10. Notes & Nepali Unicode
- Quick notes with Nepali transliteration keyboard hint (bundled Roman→Devanagari mapping, local).

### 4.3 v2.0 — On-Device AI (ONNX Runtime Android)

All models quantized (int8), downloaded on first use via **Google Play Asset Delivery / on-demand module**, stored locally. No inference ever leaves the device.

| ID | Feature | Model (ONNX) | Size target |
|---|---|---|---|
| A1 | **Devanagari OCR** — scan printed miti/date/document text → editable text; auto-detect BS dates in images and offer to add reminder | Pre-trained Devanagari text-recognition CNN (e.g., converted from open datasets: Devanagari Handwritten Character Dataset-trained CRNN), int8 | ≤ 15 MB |
| A2 | **Nepali Voice Notes / voice input** — offline speech-to-text for quick notes & reminders | Whisper-tiny (multilingual, includes Nepali) int8 ONNX, or Vosk-np model alternative | ≤ 45 MB |
| A3 | **Smart date parser** — type/voice "aaithebar Dashain ko din" → creates event | Small seq2seq/intent model distilled locally, or rule+NER hybrid; falls back to rules | ≤ 10 MB |

- Inference: `onnxruntime-android` (AAR, ~10 MB, lazy-loaded).
- Fallback: if model not downloaded, feature hidden with "Download (xx MB)" chip. App never breaks without them.

## 5. Architecture (Serverless / Local-First)

```
┌─────────────────────────────────────────────────────┐
│  UI: Jetpack Compose, Material 3, Dynamic Color      │
│  NP font stack (Kalimati/Mukta bundled subset)       │
├─────────────────────────────────────────────────────┤
│  ViewModel (StateFlow)                               │
├───────────────┬─────────────────┬───────────────────┤
│ Domain layer  │                 │                   │
│ - BsCalendar  │                 │                   │
│ - PanchangCalc│                 │                   │
│ - Converter   │                 │                   │
├───────────────┼─────────────────┼───────────────────┤
│ Room DB       │ Repositories    │ ONNX Engine       │
│ (prepopulated │ - RatesRepo     │ - OCR             │
│  calendar     │ - WeatherRepo   │ - ASR             │
│  tables)      │  (OkHttp/Retro) │ - DateParser      │
├───────────────┴─────────────────┴───────────────────┤
│ WorkManager: rate/weather sync (12h/6h, metered-ok)  │
│ DataStore: settings (theme, language NP/EN, location)│
└─────────────────────────────────────────────────────┘
```

- **Single Activity**, Compose Navigation.
- **Hilt** DI; **Coroutines + Flow**.
- Baseline Profiles + R8 full mode for startup/size.

## 6. Tech Stack (all free/open)

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin 2.x | standard |
| UI | Jetpack Compose + M3 | modern, small teams |
| Storage | Room + DataStore | prepopulated DB asset |
| Sync | WorkManager + OkHttp | only 2 endpoints hit |
| ML | ONNX Runtime Android | user requirement, MIT license |
| Astro math | Self-implemented (Meeus algorithms, Apache-2.0 reference implementations) | avoids GPL ephemeris libs |
| Fonts | Mukta / Noto Sans Devanagari (OFL) | free licensing |
| CI | GitHub Actions free tier | free |

## 7. Data Sources & Licensing

| Data | Source | Cost | Network? |
|---|---|---|---|
| BS calendar 1975–2100, festivals, holidays | Bundled static dataset (compiled from publicly published patro tables) | Free | Never |
| Tithi/nakshatra/yoga/karan | On-device astronomical computation | Free | Never |
| Sunrise/sunset | NOAA algorithm on-device | Free | Never |
| Currency rates | `open.er-api.com/v6/latest/NPR` (no key) | Free | Optional, cached |
| Weather | Open-Meteo (no key) | Free | Optional, cached |
| District/city coords | Bundled JSON (public gazetteer) | Free | Never |
| OCR/ASR models | Public pretrained weights converted to ONNX int8 | Free | One-time download |

**Rule: any network call is optional enhancement. Core app = airplane-mode proof.**

## 8. Data Model (key tables)

```
CalendarDay(bsYear, bsMonth, bsDay, adDate, weekday, tithiIndex,
            nakshatraIndex, festivalIds, holidayType)
Festival(id, nameNp, nameEn, descNp, descEn, type, recurringRule)
Event(id, title, dateBs, remindAtUtc, repeatRule, note, source[USER|OCR|VOICE])
Rate(code, nprPerUnit, updatedAtUtc)
WeatherCache(cityId, jsonPayload, fetchedAtUtc)
Settings(theme, language, homeDistrict, aiModelsInstalled)
```

BS month lengths stored as compact per-year arrays (`int[12]`) — ~126 years × 12 ints ≈ trivial size.

## 9. Performance & Quality Targets

| Metric | Target |
|---|---|
| APK (base, no models) | < 25 MB |
| RAM steady-state | < 120 MB |
| Cold start (Moto E-class) | < 1.2 s |
| Calendar scroll (year jump) | < 100 ms |
| Panchang compute per day | < 5 ms |
| Battery: background work | ≤ 2 syncs/day, JobScheduler-constrained |
| Min device | Android 8.0, 2 GB RAM |

Accuracy gate: calendar/tithi output must match published patros for all of 2081–2083 BS sample set (≥ 300 spot checks) before release; mismatch = release blocker.

## 10. Privacy

- No analytics SDKs, no ads SDKs, no crash reporters that phone home (use ACRA optional self-host later, off by default).
- Location: coarse/manual district selection preferred; GPS only if user opts in for sunrise accuracy.
- All AI inference on-device; mic used only during active dictation session.
- Play Data Safety form: "No data collected."

## 11. Localization

- Full np-NP + en default; string extraction from day one (`values-ne`).
- Nepali numerals toggle (१२३ vs 123) app-wide.

## 12. Milestones

| Phase | Scope | Exit criteria |
|---|---|---|
| M1 (wk 1–3) | Calendar core + dataset pipeline + converter | 3-year accuracy audit passed |
| M2 (wk 4–5) | Panchang engine + reminders | Notifications fire correctly across DST-less NPT edge cases |
| M3 (wk 6–7) | Currency + units + age tools | Offline-first behavior verified |
| M4 (wk 8) | Rashifal engine + polish + i18n | Internal beta (Play internal testing) |
| M5 (wk 9–11) | ONNX OCR + voice notes modules | Feature works offline on 2GB device |
| M6 | Play release (free, open-source repo) | Public launch |

Distribution: Google Play (free) + F-Droid-friendly FOSS build (no proprietary blobs; Play-only pieces like play asset delivery get a plain download-from-GitHub-Releases fallback — also free).

## 13. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Calendar dataset errors (biggest risk) | Cross-validate ≥3 published sources; community issue reporting; versioned dataset updates via app update (still no server) |
| Panchang math divergence at boundary days | Golden-file tests against Drik-computed references for 50 random dates/year |
| er-api downtime | 30-day stale cache acceptable + INR peg constant; show staleness honestly |
| Whisper-tiny Nepali quality is mediocre | Position as "quick voice notes", not transcription; keep rule-based fallback |
| ONNX AAR size creep | Dynamic feature delivery, abi splits (arm64-v8a priority) |
| Scope creep toward Hamro-Patro-everything | Non-goals section enforced ruthlessly |

## 14. Success Metrics (all measured locally/aggregate store stats, not tracking)

- Crash-free sessions > 99.5% (Play Console vitals — free).
- DAU/installs ratio > 35% (calendar apps are habitual).
- Median session 20–40s (utility pattern).
- Store rating ≥ 4.5 within 6 months.

## 15. Open Questions

1. Branding/name availability on Play Store ("Patro" namespace is crowded).
2. Include Gorkha/Bikram-era historical dates beyond 1975 BS?
3. Priority of A2 (ASR) vs A3 (smart parser) if timeline slips — recommend shipping OCR first (highest perceived value).
