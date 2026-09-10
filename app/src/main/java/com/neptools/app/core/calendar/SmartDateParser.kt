package com.neptools.app.core.calendar

import java.time.LocalDate

sealed class DetectedDate {
    data class Bs(val date: NepaliDate, val originalText: String, val description: String) : DetectedDate()
    data class Ad(val date: LocalDate, val originalText: String, val description: String) : DetectedDate()
}

object SmartDateParser {

    private val nepaliMonthsMap = mapOf(
        "बैशाख" to 1, "वैशाख" to 1, "baishakh" to 1, "baisakh" to 1,
        "जेठ" to 2, "ज्येष्ठ" to 2, "jestha" to 2, "jeth" to 2,
        "असार" to 3, "आषाढ" to 3, "ashadh" to 3, "asar" to 3,
        "साउन" to 4, "श्रावण" to 4, "shrawan" to 4, "saun" to 4,
        "भदौ" to 5, "भाद्र" to 5, "bhadra" to 5, "bhadau" to 5,
        "असोज" to 6, "आश्विन" to 6, "ashwin" to 6, "asoj" to 6,
        "कार्तिक" to 7, "कात्तिक" to 7, "kartik" to 7,
        "मंसिर" to 8, "मार्ग" to 8, "mangsir" to 8, "mamsir" to 8,
        "पुस" to 9, "पौष" to 9, "poush" to 9, "pus" to 9,
        "माघ" to 10, "magh" to 10,
        "फागुन" to 11, "फाल्गुन" to 11, "falgun" to 11, "phagun" to 11,
        "चैत" to 12, "चैत्र" to 12, "chaitra" to 12, "chait" to 12
    )

    private val englishMonthsMap = mapOf(
        "january" to 1, "jan" to 1,
        "february" to 2, "feb" to 2,
        "march" to 3, "mar" to 3,
        "april" to 4, "apr" to 4,
        "may" to 5,
        "june" to 6, "jun" to 6,
        "july" to 7, "jul" to 7,
        "august" to 8, "aug" to 8,
        "september" to 9, "sep" to 9, "sept" to 9,
        "october" to 10, "oct" to 10,
        "november" to 11, "nov" to 11,
        "december" to 12, "dec" to 12
    )

    private fun isLikelyBs(year: Int, input: String): Boolean {
        if (input.contains(Regex("""\b(ad|a\.d\.|gregorian|ce)\b""", RegexOption.IGNORE_CASE))) {
            return false
        }
        if (input.contains(Regex("""\b(bs|b\.s\.|वि\.सं\.|गते|नेपाल)\b""", RegexOption.IGNORE_CASE))) {
            return true
        }
        // In a Nepali calendar utility app, years 2050..2100 default to BS (e.g. 2081 BS).
        // Years 1920..2049 default to AD unless explicitly marked BS.
        return year in 2050..2100
    }

    fun detectDate(input: String): DetectedDate? {
        if (input.isBlank()) return null
        val normalized = NepaliNames.toAscii(input.trim())

        // 1. Check YYYY[-/.]MM[-/.]DD (e.g. 2081/05/15 or 2028-11-20)
        val ymdRegex = Regex("""\b(19\d\d|20\d\d|2100)[-/.](0?[1-9]|1[0-2])[-/.](0?[1-9]|[12]\d|3[0-2])\b""")
        val ymdMatch = ymdRegex.find(normalized)
        if (ymdMatch != null) {
            val (yStr, mStr, dStr) = ymdMatch.destructured
            val y = yStr.toInt()
            val m = mStr.toInt()
            val d = dStr.toInt()
            return if (isLikelyBs(y, input)) {
                DetectedDate.Bs(NepaliDate(y, m, d), ymdMatch.value, "वि.सं. $y/$m/$d")
            } else {
                try {
                    DetectedDate.Ad(LocalDate.of(y, m, d), ymdMatch.value, "AD $y-$m-$d")
                } catch (_: Exception) {
                    null
                }
            }
        }

        // 2. Check DD[-/.]MM[-/.]YYYY (e.g. 15/05/2081 or 20-11-2028)
        val dmyRegex = Regex("""\b(0?[1-9]|[12]\d|3[0-2])[-/.](0?[1-9]|1[0-2])[-/.](19\d\d|20\d\d|2100)\b""")
        val dmyMatch = dmyRegex.find(normalized)
        if (dmyMatch != null) {
            val (dStr, mStr, yStr) = dmyMatch.destructured
            val y = yStr.toInt()
            val m = mStr.toInt()
            val d = dStr.toInt()
            return if (isLikelyBs(y, input)) {
                DetectedDate.Bs(NepaliDate(y, m, d), dmyMatch.value, "वि.सं. $y/$m/$d")
            } else {
                try {
                    DetectedDate.Ad(LocalDate.of(y, m, d), dmyMatch.value, "AD $y-$m-$d")
                } catch (_: Exception) {
                    null
                }
            }
        }

        // 3. Check for text month formats (e.g. "15 Falgun 2081", "Falgun 15, 2081", "15 फागुन २०८१")
        for ((monthName, monthNum) in nepaliMonthsMap) {
            val pattern1 = Regex("""\b(0?[1-9]|[12]\d|3[0-2])\s*(?:गते)?\s*${Regex.escape(monthName)}\s*(19\d\d|20\d\d|2100)\b""", RegexOption.IGNORE_CASE)
            val m1 = pattern1.find(normalized)
            if (m1 != null) {
                val (dStr, yStr) = m1.destructured
                return DetectedDate.Bs(NepaliDate(yStr.toInt(), monthNum, dStr.toInt()), m1.value, "वि.सं. ${yStr.toInt()}/$monthNum/${dStr.toInt()}")
            }

            val pattern2 = Regex("""\b${Regex.escape(monthName)}\s*(0?[1-9]|[12]\d|3[0-2])\s*(?:गते)?,?\s*(19\d\d|20\d\d|2100)\b""", RegexOption.IGNORE_CASE)
            val m2 = pattern2.find(normalized)
            if (m2 != null) {
                val (dStr, yStr) = m2.destructured
                return DetectedDate.Bs(NepaliDate(yStr.toInt(), monthNum, dStr.toInt()), m2.value, "वि.सं. ${yStr.toInt()}/$monthNum/${dStr.toInt()}")
            }
        }

        // 4. Check for English month names (e.g. "15 August 2024" or "August 15, 2024")
        for ((monthName, monthNum) in englishMonthsMap) {
            val pattern1 = Regex("""\b(0?[1-9]|[12]\d|3[01])\s+${Regex.escape(monthName)}\s+(19\d\d|20\d\d)\b""", RegexOption.IGNORE_CASE)
            val m1 = pattern1.find(normalized)
            if (m1 != null) {
                val (dStr, yStr) = m1.destructured
                return try {
                    DetectedDate.Ad(LocalDate.of(yStr.toInt(), monthNum, dStr.toInt()), m1.value, "AD ${yStr.toInt()}-$monthNum-${dStr.toInt()}")
                } catch (_: Exception) {
                    null
                }
            }

            val pattern2 = Regex("""\b${Regex.escape(monthName)}\s+(0?[1-9]|[12]\d|3[01]),?\s+(19\d\d|20\d\d)\b""", RegexOption.IGNORE_CASE)
            val m2 = pattern2.find(normalized)
            if (m2 != null) {
                val (dStr, yStr) = m2.destructured
                return try {
                    DetectedDate.Ad(LocalDate.of(yStr.toInt(), monthNum, dStr.toInt()), m2.value, "AD ${yStr.toInt()}-$monthNum-${dStr.toInt()}")
                } catch (_: Exception) {
                    null
                }
            }
        }

        return null
    }
}
