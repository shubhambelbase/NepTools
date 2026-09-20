package com.neptools.app.core.calendar

import java.time.LocalDate

@androidx.compose.runtime.Immutable
data class NepaliDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String = "$year-$month-$day"
}

@androidx.compose.runtime.Immutable
data class Festival(
    val day: Int,
    val nameNp: String,
    val nameEn: String,
    val isPublicHoliday: Boolean
)

object NepaliNames {
    val monthsNp = listOf(
        "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
        "कार्तिक", "मंसिर", "पुस", "माघ", "फागुन", "चैत"
    )
    val monthsEn = listOf(
        "Baishakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )
    val adMonthsNp = listOf(
        "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
        "जुलाई", "अगस्ट", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
    )
    val adMonthsNpShort = listOf(
        "जन", "फेब", "मार्च", "अप्रि", "मे", "जुन",
        "जुल", "अग", "सेप्ट", "अक्टो", "नोभे", "डिसे"
    )
    val weekdaysNp = listOf("आइतबार", "सोमबार", "मङ्गलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार")
    val weekdaysNpShort = listOf("आइत", "सोम", "मंगल", "बुध", "बिही", "शुक्र", "शनि")
    val weekdaysEn = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val weekdaysEnShort = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private const val digits = "०१२३४५६७८९"

    fun toDevanagari(value: Int): String =
        value.toString().map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")

    fun toDevanagari(value: Long): String =
        value.toString().map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")

    fun toDevanagari(value: String): String =
        value.map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")

    fun toAscii(value: String): String =
        value.map { c ->
            val idx = digits.indexOf(c)
            if (idx >= 0) ('0' + idx) else c
        }.joinToString("")
}

object WeekdayMapper {
    fun fromJava(dayOfWeek: java.time.DayOfWeek): Int = (dayOfWeek.value % 7)
}
