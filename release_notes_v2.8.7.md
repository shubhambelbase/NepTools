### NepTools v2.8.7 (versionCode 46) - Release Notes

#### Highlights & Improvements
- **Clean Graphics & PDF Output**:
  - Removed promotional footers, watermarks, "NepTools 100% Ad-Free • Privacy-First", website URLs, and copyright strings from Patro and Rashifal graphic cards (`PatroGraphicGenerator.kt`).
  - Removed promotional watermark from bill receipts (`ReceiptGraphicGenerator.kt`).
  - Cleaned Kundali and Guna Milan astrology reports (`AstroPdfExporter.kt`), removing software branding and website links while preserving authentic astrological ephemeris notes.
  - Removed software prefixes and promotional footer watermarks from official government application letters (`PdfExporter.kt`), delivering clean, professional printable documents.
- **Unbranded Social Sharing**:
  - Removed promotional messages, taglines, and GitHub URLs from `ACTION_SEND` intents across card sharing, bill splitters, land converter, and loan EMI calculators.
  - When sharing graphics, only the clean image is delivered without appended links or marketing text.

#### Production Verification
- SHA-256: PENDING_CALCULATION
- Size: ~9.3 MB
- Verified with R8 full-mode shrinking, ProGuard optimizations, and C++20 native security.
